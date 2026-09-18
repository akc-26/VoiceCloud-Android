plugins { alias(libs.plugins.android.library) }

android {
    namespace = "app.voicecloud.core.preferences"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(libs.kotlinx.coroutines.android)
    api(libs.androidx.datastore.preferences)
}
