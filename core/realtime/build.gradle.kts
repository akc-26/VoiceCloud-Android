plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "app.voicecloud.core.realtime"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core:security"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.socketio.client) {
        exclude(group = "org.json", module = "json")
    }
}
