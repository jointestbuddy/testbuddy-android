# TestBuddy Android SDK

The SDK of [TestBuddy](https://jointestbuddy.com), where Android developers test each other's apps to pass Google Play's closed test (12 testers for 14 days). It links each install to a tester's place and measures how long the app stays in the foreground, so the developer gets real engagement numbers for Google's production access questionnaire.

- Kotlin, callable from Java. minSdk 24.
- One dependency: Google's Play Install Referrer client. No AndroidX, no user interface.
- Full documentation, including what it sends and what it never sends: [jointestbuddy.com/sdk](https://jointestbuddy.com/sdk/).

## Install

The SDK is on Maven Central and starts on its own from a key in your manifest: no repository to add, no code. In your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.jointestbuddy:testbuddy-android:0.3.0")
}
```

In `AndroidManifest.xml`, inside `<application>`, with the key shown in TestBuddy's setup (it starts with `pk_`):

```xml
<meta-data android:name="com.jointestbuddy.KEY" android:value="pk_..." />
```

The steps for Flutter, React Native, Expo (a config plugin), Unity and Capacitor are on [jointestbuddy.com/sdk](https://jointestbuddy.com/sdk/). Prefer code? `TestBuddy.init(this, "pk_...")` in `Application.onCreate` works too.

Keep it in every build you upload to your closed test during your wave. You can remove it before your production release.

## Repository

`sdk/` is the library, `demo/` a small app that stands in for a tested app. Tests: `./gradlew :sdk:testDebugUnitTest`. Releases go to Maven Central (`com.jointestbuddy`).

This repository is published from TestBuddy's main repository; issues and questions are welcome here or at hello@jointestbuddy.com.

## License

Apache License 2.0, see [LICENSE](LICENSE).

Generated with Claude Code, courtesy of lemlist
