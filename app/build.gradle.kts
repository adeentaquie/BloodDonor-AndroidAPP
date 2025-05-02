// Top-level build.gradle.kts for the app module

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // ✅ Firebase plugin
}

android {
    namespace = "com.example.blooddonor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.blooddonor"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Inject MAPS_API_KEY from local.properties
        val mapsApiKey: String? = project.findProperty("MAPS_API_KEY") as String?
        if (mapsApiKey != null) {
            buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
        }

        // ✅ This line fixes the manifest merger issue
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey ?: ""
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
}

dependencies {
    // ✅ Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // ✅ Firebase dependencies (no version numbers)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")

    // Google Play Services + Maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.libraries.places:places:3.3.0")

    // Jetpack + UI libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
