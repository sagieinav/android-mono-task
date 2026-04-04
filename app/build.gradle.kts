plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.sagi.monotask"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.sagi.monotask"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.addAll(listOf("-opt-in=kotlin.time.ExperimentalTime"))
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.concurrent:concurrent-futures:1.2.0")
        force("androidx.concurrent:concurrent-futures-ktx:1.2.0")
    }
}

dependencies {
    // ========== APP DESIGN SYSTEM MODULE ==========
    implementation(project(":designsystem"))

    // ========== UI ==========
    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Android Splash screen (for app launch)
    implementation(libs.androidx.core.splashscreen)

    // Coil (Native Image Loader)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Haze (library for blur effects)
    implementation(libs.haze)
    implementation(libs.haze.materials)

    // Wheel Picker
    implementation(libs.datetime.wheel.picker)
    implementation(libs.kotlinx.datetime)



    // ========== Core / Non-UI ==========
    // Coroutines & Lifecycle
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // DataStore (modern SharedPreferences)
    implementation(libs.androidx.datastore.preferences)

    // Google Services Auth
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt (Dependency Injection)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)


    // ========== TEST & DEBUG ==========
    // Compose
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
//    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Unit Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Concurrent Futures
    androidTestImplementation(libs.androidx.concurrent.futures)
    androidTestImplementation(libs.androidx.concurrent.futures.ktx)
}
