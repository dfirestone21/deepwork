val composeVersion by extra("1.5.4")
val kotlinVersion by extra("1.9.20")
val roomVersion by extra("2.6.1")
val lifecycleVersion by extra("2.7.0")
val mockkVersion by extra("1.13.2")
val coroutinesVersion by extra("1.6.0")
val hiltVersion by extra("2.51.1")

buildscript {

}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.deepwork"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.deepwork"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes.addAll(
                listOf("META-INF/DEPENDENCIES", "META-INF/LICENSE", "META-INF/LICENSE.md", "META-INF/LICENSE-notice.md")
            )
        }
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("io.mockk:mockk-android:${mockkVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${coroutinesVersion}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${coroutinesVersion}")

    // hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}