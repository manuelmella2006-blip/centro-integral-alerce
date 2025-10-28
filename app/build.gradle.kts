plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.centrointegralalerce"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.centrointegralalerce"
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
    // DEPENDENCIAS BÁSICAS (YA LAS TIENES)
    // ============================================
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ============================================
    // FIREBASE (YA LO TIENES - PERFECTO)
    // ============================================
    // Firebase BoM - controla todas las versiones
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // Firebase - sin especificar versiones (las gestiona el BoM)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // ============================================
    // ⚠️ DEPENDENCIAS CRÍTICAS - COPIAR TODAS ⚠️
    // ============================================

    // ViewPager2 - CRÍTICO para el calendario
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // SwipeRefreshLayout - CRÍTICO para pull-to-refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

    // RecyclerView - Necesario para el calendario
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // CardView - Para los cards del calendario
    implementation("androidx.cardview:cardview:1.0.0")

    // Fragment - Para navegación mejorada
    implementation("androidx.fragment:fragment-ktx:1.8.5")

    // Lifecycle - Para ActivityResultLauncher
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
}