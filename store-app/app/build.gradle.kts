plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.connecto.store"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.connecto.store"
        minSdk = 26
        targetSdk = 37
        versionCode = 33
        versionName = "1.7.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("connecto-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "connecto2026"
            keyAlias = System.getenv("KEY_ALIAS") ?: "connecto"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "connecto2026"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "BASE_URL", "\"https://connecto.fun\"")
        }
        debug {
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "BASE_URL", "\"https://connecto.fun\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
}
