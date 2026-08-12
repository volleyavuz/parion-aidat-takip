plugins { id("com.android.application") }

android {
    namespace = "com.parion.aidat"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.parion.aidat"
        minSdk = 23
        targetSdk = 36
        versionCode = 3
        versionName = "2.1-pilot"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
}
