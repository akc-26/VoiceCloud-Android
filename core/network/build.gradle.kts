plugins { alias(libs.plugins.android.library) }

android {
    namespace = "app.voicecloud.core.network"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":core:model"))
    api(project(":core:security"))
    api(libs.retrofit.core)
    api(libs.moshi.kotlin)

    implementation(libs.retrofit.moshi)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit4)
}
