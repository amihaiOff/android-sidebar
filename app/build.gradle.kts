import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// --- Stable signing ---------------------------------------------------------
// The committed base64 keystore is decoded to a real keystore file so both CI
// and in-session builds sign with ONE fixed signature. That means every new
// APK installs as an update over the previous one, and the "Draw over other
// apps" permission is granted once, ever, and persists across iterations.
val keystoreB64 = rootProject.file("keystore/debug.keystore.base64")
val keystoreFile = rootProject.file("keystore/debug.keystore")
if (keystoreB64.exists() && !keystoreFile.exists()) {
    keystoreFile.writeBytes(Base64.getMimeDecoder().decode(keystoreB64.readText()))
}

android {
    namespace = "com.personal.sidebar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.personal.sidebar"
        minSdk = 29
        targetSdk = 35
        // CI passes the run number so every build has a higher versionCode:
        // Android then treats each new APK as an update and Obtainium notices it.
        val ciVersion = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionCode = ciVersion
        versionName = "1.0.$ciVersion"
    }

    signingConfigs {
        // Single stable key used for ALL build types (see note above).
        create("stable") {
            storeFile = keystoreFile
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("stable")
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("stable")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.savedstate)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)
}
