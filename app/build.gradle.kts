plugins { id("com.android.application") }
android {
    namespace = "com.parion.aidat"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.parion.aidat"
        minSdk = 23
        targetSdk = 36
        versionCode = 15
        versionName = "3.7-pilot"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
