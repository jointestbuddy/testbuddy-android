# TestBuddy Android SDK

The SDK of [TestBuddy](https://jointestbuddy.com), where Android developers test each other's apps to pass Google Play's closed test (12 testers for 14 days). It links each install to a tester's place and measures how long the app stays in the foreground, so the developer gets real engagement numbers for Google's production access questionnaire.

- Kotlin, callable from Java. minSdk 24.
- One dependency: Google's Play Install Referrer client. No AndroidX, no user interface.
- Full documentation, including what it sends and what it never sends: [jointestbuddy.com/sdk](https://jointestbuddy.com/sdk/).

## Install

In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

In your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.jointestbuddy:testbuddy-android:0.1.0")
}
```

In `Application.onCreate`, with the key shown in TestBuddy's setup (it starts with `pk_`):

```kotlin
import com.jointestbuddy.TestBuddy

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        TestBuddy.init(this, "pk_...")
    }
}
```

Keep it in every build you upload to your closed test during your wave. You can remove it before your production release.

## Repository

`sdk/` is the library, `demo/` a small app that stands in for a tested app. Tests: `./gradlew :sdk:testDebugUnitTest`. JitPack builds each tag with `jitpack.yml`.

This repository is published from TestBuddy's main repository; issues and questions are welcome here or at hello@jointestbuddy.com.

## License

Apache License 2.0, see [LICENSE](LICENSE).

Generated with Claude Code, courtesy of lemlist
