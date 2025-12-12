plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.root.datamanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.root.datamanager"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // এই অংশটুকু মিসিং ছিল, যা ভার্সন ঠিক করবে
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_17
        targetCompatibility = JavaVersion.VERSION_1_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}
