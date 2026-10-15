plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "com.jointestbuddy"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// JitPack builds the public SDK repository and serves com.github.jointestbuddy:testbuddy-android:<tag>.
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.jointestbuddy"
            artifactId = "testbuddy-android"
            version = "0.2.0"
            afterEvaluate { from(components["release"]) }
        }
    }
}

dependencies {
    implementation("com.android.installreferrer:installreferrer:2.2")
    testImplementation("junit:junit:4.13.2")
}
