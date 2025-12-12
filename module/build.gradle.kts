plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// এখানেও Toolchain যোগ করা হলো
kotlin {
    jvmToolchain(17)
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
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
