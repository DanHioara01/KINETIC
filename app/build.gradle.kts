import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties()
if (keystorePropsFile.exists()) {
    keystoreProps.load(FileInputStream(keystorePropsFile))
}

android {
    namespace = "com.example.kinetic"
    compileSdk = 35

    signingConfigs {
        create("release") {
            if (keystorePropsFile.exists()) {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.example.kinetic"
        minSdk = 24
        targetSdk = 35
        versionCode = 30
        versionName = "3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // AdMob rewarded ad unit ID (replace with production unit before release)
        buildConfigField("String", "ADMOB_REWARDED_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // java.time (LocalDate etc.) needs core library desugaring on API < 26
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Room database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ViewModel Compose
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")

    // Google Sign-In
    implementation(libs.play.services.auth)

    // Facebook SDK
    implementation(libs.facebook.login)

    // Coil (image loading)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // Retrofit (networking)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    // Google Code Scanner (no native .so, 16KB compatible)
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    // OkHttp (for OpenFoodFacts)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // OSMDroid (satellite map)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // WorkManager (background sync)
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Jetpack Glance (Compose app widgets)
    implementation(libs.glance.appwidget)

    // Core library desugaring (java.time on API 24/25)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    // Google Play Services Location (GPS tracking)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // RevenueCat (Google Play Billing wrapper + UI Paywall/Customers)
    implementation(libs.revenuecat.purchases)
    implementation(libs.revenuecat.purchases.ui)

    // AdMob (rewarded ads)
    implementation(libs.play.services.ads)

    // Firestore coroutines (kotlinx-coroutines-play-services)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Haze (frosted glass blur)
    implementation("dev.chrisbanes.haze:haze:1.5.4")
    implementation("dev.chrisbanes.haze:haze-materials:1.5.4")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
}