plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.connecto"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.connecto.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 41
        versionName = "3.9.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"https://connecto.fun\"")
        buildConfigField("String", "WS_BASE_URL", "\"wss://connecto.fun\"")
    }

    buildTypes {
        debug {
            // Pointing directly to production server hosted on box (100.87.184.30) via Cloudflare
            buildConfigField("String", "BASE_URL", "\"https://connecto.fun\"")
            buildConfigField("String", "WS_BASE_URL", "\"wss://connecto.fun\"")
        }
        release {
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            // Real production backend configuration (Strict HTTPS & WSS)
            buildConfigField("String", "BASE_URL", "\"https://connecto.fun\"")
            buildConfigField("String", "WS_BASE_URL", "\"wss://connecto.fun\"")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Firebase Cloud Messaging for native push notifications
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation("io.getstream:stream-webrtc-android:1.1.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}