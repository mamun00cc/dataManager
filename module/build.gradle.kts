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
    
    kotlin {
        jvmToolchain(17)
    }

    packaging {
        resources {
            excludes += "META-INF/**" 
        }
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
    // FTP Server Core
    implementation("org.apache.ftpserver:ftpserver-core:1.2.0")
    // Logging (Required by FtpServer)
    implementation("org.slf4j:slf4j-simple:1.7.30")
}
