plugins {
    id("com.android.library")
    id("maven-publish")
    id("signing")
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
            withJavadocJar()
        }
    }
}

// Two homes, the same library: JitPack builds the public repository's tags as com.github.jointestbuddy, and
// scripts/publish-central.sh builds the signed bundle for Maven Central as com.jointestbuddy (-Pcentral).
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = if (project.hasProperty("central")) "com.jointestbuddy" else "com.github.jointestbuddy"
            artifactId = "testbuddy-android"
            version = "0.3.0"
            afterEvaluate { from(components["release"]) }
            pom {
                name.set("TestBuddy Android SDK")
                description.set("Links tester installs and measures real use for Google Play's closed test, with TestBuddy.")
                url.set("https://jointestbuddy.com/sdk/")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("testbuddy")
                        name.set("TestBuddy")
                        email.set("hello@jointestbuddy.com")
                        url.set("https://jointestbuddy.com")
                    }
                }
                scm {
                    url.set("https://github.com/jointestbuddy/testbuddy-android")
                    connection.set("scm:git:https://github.com/jointestbuddy/testbuddy-android.git")
                }
            }
        }
    }
    repositories {
        // The Central bundle is built here, then zipped and uploaded by scripts/publish-central.sh.
        maven {
            name = "centralBundle"
            url = uri(layout.buildDirectory.dir("central-bundle"))
        }
    }
}

// Signing is for the Central bundle only: the script passes the key from the Keychain (JitPack builds unsigned).
signing {
    val key = findProperty("signingKey") as String?
    if (key != null) {
        useInMemoryPgpKeys(key, findProperty("signingPassword") as String?)
        sign(publishing.publications["release"])
    }
}

dependencies {
    implementation("com.android.installreferrer:installreferrer:2.2")
    testImplementation("junit:junit:4.13.2")
}
