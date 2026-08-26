plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "app.voicecloud.core.database"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(libs.androidx.room.runtime)
    api(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
