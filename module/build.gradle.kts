plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.root.datamanager.module"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.root.datamanager.module"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    
    // এখানেও একই ফিক্স
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_17
        targetCompatibility = JavaVersion.VERSION_1_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
}
