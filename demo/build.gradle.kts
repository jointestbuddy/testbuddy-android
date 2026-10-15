plugins {
    id("com.android.application")
}

android {
    namespace = "com.jointestbuddy.demo"
    compileSdk = 37

    defaultConfig {
        // The seed gives this package to Sourdough Timer, so the demo plays that app in the local wave.
        applicationId = "com.jointestbuddy.demo"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        // 10.0.2.2 is the Mac as seen from the emulator; pk_local_demo is the key `npm run seed` sets.
        // The demo uses the SDK with no code: the key and the API reach it through the manifest.
        debug {
            manifestPlaceholders["testbuddyKey"] = "pk_local_demo"
            manifestPlaceholders["testbuddyApi"] = "http://10.0.2.2:3005"
            buildConfigField("String", "API_URL", "\"http://10.0.2.2:3005\"")
        }
        release {
            manifestPlaceholders["testbuddyKey"] = ""
            manifestPlaceholders["testbuddyApi"] = ""
            buildConfigField("String", "API_URL", "\"https://jointestbuddy.com\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":sdk"))
}
