plugins { id("com.android.application") }

android {
    namespace = "com.parion.aidat"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.parion.aidat"
        minSdk = 23
        targetSdk = 36
        versionCode = 1032
        versionName = "4.1.47"
        // Restore 3/6/12 month payment report picker and add direct parent WhatsApp sharing.
    }
    signingConfigs { create("release") { storeFile = file(System.getenv("PARION_KEYSTORE_PATH") ?: "parion-release.jks"); storePassword = System.getenv("PARION_STORE_PASSWORD"); keyAlias = System.getenv("PARION_KEY_ALIAS"); keyPassword = System.getenv("PARION_KEY_PASSWORD") } }
    buildTypes { getByName("release") { isMinifyEnabled = false; signingConfig = signingConfigs.getByName("release") } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}

dependencies {
    implementation("androidx.work:work-runtime:2.10.1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
}
