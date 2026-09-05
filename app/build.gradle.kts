plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.anchor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.anchor"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            // Local unit tests run against the android.jar stub; without this,
            // any call into it (e.g. android.util.Log) throws instead of no-op'ing.
            isReturnDefaultValues = true
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // DM Sans / DM Mono / Newsreader, resolved on-device via the Google
    // Fonts provider (Play services font cache) — no bundled font binaries,
    // falls back to the system font while resolving or if unavailable.
    implementation("androidx.compose.ui:ui-text-google-fonts")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // Home-screen widget trigger (grounding feature).
    implementation("androidx.glance:glance-appwidget:1.1.0")

    // Single-photo capture for the grounding feature. camera-view supplies
    // PreviewView, used only to give the camera pipeline a real Surface —
    // never shown at meaningful size, never surfaced to the user.
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // Bundled (statically-linked) on-device model: works immediately,
    // fully offline, no first-run download — see ObjectLabelerFactory.
    implementation("com.google.mlkit:image-labeling:17.0.9")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
}
