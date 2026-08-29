plugins { id("com.android.application") }

android {
    namespace = "com.parion.aidat"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.parion.aidat"
        minSdk = 23
        targetSdk = 36
        versionCode = 1071
        versionName = "4.2.31"
        // Keep V657 as the only visible dashboard; legacy V36 finance cards are compatibility no-ops.
    }
    signingConfigs { create("release") { storeFile = file(System.getenv("PARION_KEYSTORE_PATH") ?: "parion-release.jks"); storePassword = System.getenv("PARION_STORE_PASSWORD"); keyAlias = System.getenv("PARION_KEY_ALIAS"); keyPassword = System.getenv("PARION_KEY_PASSWORD") } }
    buildTypes { getByName("release") { isMinifyEnabled = false; signingConfig = signingConfigs.getByName("release") } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}

dependencies {
    implementation("androidx.work:work-runtime:2.10.1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
