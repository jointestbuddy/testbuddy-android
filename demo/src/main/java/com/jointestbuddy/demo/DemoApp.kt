package com.jointestbuddy.demo

import android.app.Application
import com.jointestbuddy.TestBuddy

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TestBuddy.init(this, BuildConfig.CAMPAIGN_KEY, BuildConfig.API_URL)
    }
}
