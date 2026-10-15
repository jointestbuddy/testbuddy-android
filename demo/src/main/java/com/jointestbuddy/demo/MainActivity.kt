package com.jointestbuddy.demo

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.jointestbuddy.TestBuddy

/** Stands in for a tested app: shows whether the install is linked and how long this visit has lasted. */
class MainActivity : Activity() {
    private val tick = Handler(Looper.getMainLooper())
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = TextView(this).apply { textSize = 20f }
        val code = EditText(this).apply { hint = "Link code (fallback)" }
        val link = Button(this).apply {
            text = "Link"
            setOnClickListener { TestBuddy.link(code.text.toString().trim()) { runOnUiThread { render() } } }
        }
        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 160, 48, 48)
                addView(status)
                addView(code)
                addView(link)
            },
        )
    }

    override fun onStart() {
        super.onStart()
        tick.post(object : Runnable {
            override fun run() {
                render()
                tick.postDelayed(this, 1000)
            }
        })
    }

    override fun onStop() {
        tick.removeCallbacksAndMessages(null)
        super.onStop()
    }

    private fun render() {
        status.text = "Sourdough Timer (demo)\n\n" +
            (if (TestBuddy.isLinked) "Linked to TestBuddy" else "Not linked yet") +
            "\nOpen for ${TestBuddy.foregroundSeconds} s\nA day counts after 2 min; leave the app to send it."
    }
}
