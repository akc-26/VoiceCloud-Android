plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.compose); alias(libs.plugins.ksp); alias(libs.plugins.hilt) }
android { namespace = "app.voicecloud.feature.bootstrap"; compileSdk = 37; defaultConfig { minSdk = 26 }; buildFeatures { compose = true }; compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 } }
dependencies {
    api(project(":core:model"))
    api(project(":core:network"))
    api(project(":core:logging"))
    api(libs.kotlinx.coroutines.android)
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.core.ktx); implementation(libs.androidx.lifecycle.viewmodel.compose); implementation(libs.androidx.lifecycle.runtime.compose); implementation(libs.androidx.hilt.lifecycle.viewmodel.compose); implementation(libs.hilt.android); ksp(libs.hilt.compiler)
    val composeBom = platform(libs.androidx.compose.bom); implementation(composeBom); implementation(libs.androidx.compose.ui); implementation(libs.androidx.compose.foundation); implementation(libs.androidx.compose.animation); implementation(libs.androidx.compose.material3); implementation(libs.androidx.compose.ui.tooling.preview)
    testImplementation(libs.junit4); testImplementation(libs.kotlinx.coroutines.test)
}
