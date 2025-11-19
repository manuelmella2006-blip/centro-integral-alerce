plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // 🔹 Firebase plugin
}

android {
    namespace = "com.example.centrointegralalerce"
    compileSdk = 36

    defaultConfig {
        applicationId = "cl.LosMuchachos.centrointegralalerce"
        minSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // ============================================
    // 🔹 DEPENDENCIAS BASE
    // ============================================
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ============================================
    // 🔹 FIREBASE - CONTROLADO POR BoM
    // ============================================
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // 🔹 NOTIFICACIONES PUSH (FCM)
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.work:work-runtime:2.8.1")
    implementation("com.google.android.material:material:1.9.0")

    // ============================================
    // 🔹 OTRAS LIBRERÍAS NECESARIAS
    // ============================================
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // ============================================
    // ⭐ NUEVO: WORKMANAGER (para notificaciones programadas)
    // ============================================
    implementation("androidx.work:work-runtime:2.9.0")
}
