package com.jointestbuddy

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors

/**
 * TestBuddy SDK. It starts by itself from the app's manifest (see [InitProvider]), or call [init] once in
 * `Application.onCreate`: it links this install to the tester's place (Play install
 * referrer, or a link from the TestBuddy web), then reports how long the app stays in the foreground. It sends only
 * what validates a test day: an install token, session start times and durations, objective keys. No personal data.
 * Remove it before the production release.
 */
object TestBuddy {
    internal const val DEFAULT_API = "https://jointestbuddy.com"
    private const val PREFS = "com.jointestbuddy"
    private const val TOKEN = "installToken"
    private const val QUEUE = "queue"
    private const val REFERRER_READ = "referrerRead"
    // The API takes 100 events a call; older events beyond this are dropped (a day only needs its minutes).
    private const val QUEUE_MAX = 200
    private const val MAX_SESSION_S = 4 * 3600L

    private lateinit var app: Application
    private lateinit var campaignKey: String
    private lateinit var apiUrl: String
    // One thread: network and queue writes never race.
    private val io = Executors.newSingleThreadExecutor()
    private var startedActivities = 0
    private var foregroundSince = 0L

    private val prefs get() = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    internal val isInitialized get() = this::app.isInitialized

    /** True once this install is linked to a tester's place. */
    @JvmStatic
    val isLinked: Boolean
        get() = isInitialized && prefs.getString(TOKEN, null) != null

    /** Seconds the app has been in the foreground since it last came back, 0 in the background. */
    @JvmStatic
    val foregroundSeconds: Long
        get() = if (startedActivities > 0) (System.currentTimeMillis() - foregroundSince) / 1000 else 0

    /** Starts the SDK. [campaignKey] is the key shown on the TestBuddy web; [apiUrl] is for local testing only. */
    @JvmStatic
    @JvmOverloads
    fun init(application: Application, campaignKey: String, apiUrl: String = DEFAULT_API) {
        if (this::app.isInitialized) return
        app = application
        this.campaignKey = campaignKey
        this.apiUrl = apiUrl.trimEnd('/')
        background { post("/v1/sdk/hello", JSONObject().put("campaignKey", campaignKey).put("packageName", app.packageName)) }
        if (!isLinked && !prefs.getBoolean(REFERRER_READ, false)) readReferrer()
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                if (startedActivities++ == 0) {
                    foregroundSince = System.currentTimeMillis()
                    flush()
                }
            }

            override fun onActivityStopped(activity: Activity) {
                if (--startedActivities == 0) endSession()
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    /** Links this install with a place code (from the web link). [done] gets true once linked, on a background thread. */
    @JvmStatic
    @JvmOverloads
    fun link(code: String, done: (Boolean) -> Unit = {}) {
        if (!isInitialized) return done(false)
        background { done(linkNow(code) == 201) }
    }

    // On the io thread. The HTTP status of the link (201 linked, 404 unknown code), or null when the network failed.
    private fun linkNow(code: String): Int? {
        val body = JSONObject().put("campaignKey", campaignKey).put("packageName", app.packageName).put("code", code)
        val response = post("/v1/sdk/link", body) ?: return null
        val token = response.second?.takeIf { response.first == 201 }?.let { runCatching { JSONObject(it).optString("installToken") }.getOrNull() }
        if (token.isNullOrEmpty()) return if (response.first == 201) null else response.first
        prefs.edit().putString(TOKEN, token).apply()
        sendQueue()
        return 201
    }

    /** Records that the tester reached one of the campaign's objectives (a key set on the TestBuddy web). */
    @JvmStatic
    fun objective(key: String) {
        if (!isInitialized) return
        enqueue(JSONObject().put("kind", "objective").put("objective", key))
    }

    private fun readReferrer() {
        val client = InstallReferrerClient.newBuilder(app).build()
        runCatching { client.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(code: Int) {
                val referrer =
                    if (code == InstallReferrerClient.InstallReferrerResponse.OK) runCatching { client.installReferrer.installReferrer }.getOrNull() else null
                runCatching { client.endConnection() }
                val placeCode = codeFromReferrer(referrer)
                // Read for good once there is no code, or the API gave a definite answer; a network failure retries at
                // the next launch.
                if (placeCode == null) prefs.edit().putBoolean(REFERRER_READ, true).apply()
                else background { if (linkNow(placeCode).let { it == 201 || it == 404 }) prefs.edit().putBoolean(REFERRER_READ, true).apply() }
            }

            override fun onInstallReferrerServiceDisconnected() {}
        }) }
    }

    private fun endSession() {
        val seconds = ((System.currentTimeMillis() - foregroundSince) / 1000).coerceAtMost(MAX_SESSION_S)
        if (seconds < 1) return
        enqueue(JSONObject().put("kind", "session").put("durationS", seconds), at = foregroundSince)
    }

    // Events wait in the queue until this install is linked and the network answers; sessions before the link count.
    private fun enqueue(event: JSONObject, at: Long = System.currentTimeMillis()) = background {
        event.put("eventId", UUID.randomUUID().toString()).put("deviceAt", at)
        val queue = JSONArray(prefs.getString(QUEUE, "[]"))
        queue.put(event)
        while (queue.length() > QUEUE_MAX) queue.remove(0)
        prefs.edit().putString(QUEUE, queue.toString()).apply()
        sendQueue()
    }

    private fun flush() = background { sendQueue() }

    // An uncaught exception on any thread kills the host app: the SDK swallows its own failures.
    private fun background(task: () -> Unit) = io.execute { runCatching(task) }

    // On the io thread only. ponytail: retried at the next launch or session end, no WorkManager; add one if events
    // must leave while the app stays closed.
    private fun sendQueue() {
        val token = prefs.getString(TOKEN, null) ?: return
        val queue = JSONArray(prefs.getString(QUEUE, "[]"))
        if (queue.length() == 0) return
        val batch = JSONArray().apply { for (i in 0 until minOf(100, queue.length())) put(queue.getJSONObject(i)) }
        val status = post("/v1/sdk/events", JSONObject().put("events", batch), token)?.first
        when {
            // The place ended or was revoked: nothing more to send for it.
            status == 401 -> prefs.edit().remove(TOKEN).remove(QUEUE).apply()
            // Sent, or refused for good (a malformed batch must not jam the queue); 429, 5xx and no network retry.
            status == 200 || (status != null && status in 400..499 && status != 429) -> {
                val rest = JSONArray().apply { for (i in batch.length() until queue.length()) put(queue.getJSONObject(i)) }
                prefs.edit().putString(QUEUE, rest.toString()).apply()
                if (status == 200 && rest.length() > 0) sendQueue()
            }
        }
    }

    /** POST JSON; the status and body, or null when the network fails. */
    private fun post(path: String, body: JSONObject, token: String? = null): Pair<Int, String?>? = try {
        val connection = URL(apiUrl + path).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.doOutput = true
        connection.setRequestProperty("content-type", "application/json")
        token?.let { connection.setRequestProperty("authorization", "Bearer $it") }
        connection.outputStream.use { it.write(body.toString().toByteArray()) }
        val status = connection.responseCode
        val text = (if (status < 400) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }
        connection.disconnect()
        status to text
    } catch (_: Exception) {
        null
    }
}
