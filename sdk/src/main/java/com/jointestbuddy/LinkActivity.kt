package com.jointestbuddy

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

/**
 * Opened by testbuddy://<package>/link?code=XXXXXX from the TestBuddy web: links this install to the tester's place,
 * then opens the app. Invisible; nothing shows to users who never tap such a link.
 */
class LinkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val code = codeFromLink(intent?.data?.getQueryParameter("code"))
        val context = applicationContext
        // No code, or an app that never called TestBuddy.init: nothing to link, and never a crash.
        if (code == null || !TestBuddy.isInitialized) {
            finish()
            return
        }
        TestBuddy.link(code) { linked ->
            runOnUiThread {
                Toast.makeText(context, if (linked) "Linked to TestBuddy" else "This TestBuddy link did not work", Toast.LENGTH_LONG).show()
                packageManager.getLaunchIntentForPackage(packageName)?.let { startActivity(it) }
                finish()
            }
        }
    }
}
