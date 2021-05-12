plugins {
    id("com.android.application")
    id("ru.erdenian.shrinkometer")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "ru.erdenian.shrinkometer.testapp"
        versionCode = 1
        versionName = "0.0"

        minSdkVersion(21)
        targetSdkVersion(30)
    }

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("signing/debug.jks")
            storePassword = "debugdebug"
            keyAlias = "debug"
            keyPassword = "debugdebug"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("debug")
        }
    }
}
