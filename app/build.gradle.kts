plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Damit sich eine neue Version ueber die vorherige installieren laesst,
// muss jede APK mit demselben Schluessel signiert sein. Der Schluessel
// kommt aus einem GitHub-Secret und liegt nicht im Repository.
val keystoreFile = rootProject.file("keystore/unsere-tage.jks")
val keystorePassword: String? = System.getenv("KEYSTORE_PASSWORD")
val signingReady = keystoreFile.exists() && !keystorePassword.isNullOrBlank()

android {
    namespace = "com.unseretage.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.unseretage.app"
        minSdk = 26
        targetSdk = 35
        // Die Build-Nummer von GitHub Actions wird zur Versionsnummer,
        // damit die App neue Releases erkennen kann.
        val build = (System.getenv("BUILD_NUMBER") ?: "1").toIntOrNull() ?: 1
        versionCode = build
        versionName = "1.$build"
    }

    if (signingReady) {
        signingConfigs.create("shared") {
            storeFile = keystoreFile
            storePassword = keystorePassword
            keyAlias = System.getenv("KEY_ALIAS") ?: "unseretage"
            keyPassword = keystorePassword
        }
    }

    buildTypes {
        debug {
            if (signingReady) signingConfig = signingConfigs.getByName("shared")
        }
        release {
            isMinifyEnabled = false
            if (signingReady) signingConfig = signingConfigs.getByName("shared")
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
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("com.google.android.material:material:1.12.0")
}
