plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.voicecloud.core.designsystem"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    api(composeBom)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
