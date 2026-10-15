package com.jointestbuddy

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri

/**
 * Starts the SDK with no code. The app's manifest carries the key:
 * `<meta-data android:name="com.jointestbuddy.KEY" android:value="pk_..." />`, and Android creates this provider before
 * `Application.onCreate` in any app built with Gradle: native, Flutter, React Native, Expo, Unity, Capacitor. Without
 * the key it does nothing, so [TestBuddy.init] keeps working. `com.jointestbuddy.API_URL` is for local testing only.
 */
class InitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        // Never breaks the host app: a missing key or any error leaves the SDK off.
        runCatching {
            val app = context?.applicationContext as? Application ?: return false
            @Suppress("DEPRECATION")
            val meta = app.packageManager.getApplicationInfo(app.packageName, PackageManager.GET_META_DATA).metaData ?: return false
            val key = meta.getString(KEY)?.trim().orEmpty()
            if (key.isEmpty()) return false
            TestBuddy.init(app, key, meta.getString(API_URL)?.takeIf { it.isNotBlank() } ?: TestBuddy.DEFAULT_API)
        }
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0

    companion object {
        const val KEY = "com.jointestbuddy.KEY"
        const val API_URL = "com.jointestbuddy.API_URL"
    }
}
