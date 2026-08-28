import java.util.Properties
import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val brandProperties = Properties().apply {
    rootProject.file("branding/voicecloud-brand.properties").inputStream().use { load(it) }
}
fun brand(key: String): String = brandProperties.getProperty(key)?.trim().orEmpty().ifBlank {
    error("Missing white-label branding property: $key")
}



android {
    namespace = "app.voicecloud.android"
    compileSdk = 37

    defaultConfig {
        applicationId = brand("brand.applicationId")
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0-ph06"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        resValue("string", "vc_brand_app_name", brand("brand.name"))
        resValue("color", "vc_brand_launcher_background", brand("consumer.sapphire"))
        resValue("color", "vc_brand_system_surface", brand("consumer.surface"))
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }

    fun property(name: String): String = providers.gradleProperty(name).orNull.orEmpty().trim()
    fun quoted(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
    fun webHost(url: String): String = URI(url).host ?: error("VoiceCloud Web endpoint must contain a valid host: $url")

    // PH01-R08+: endpoint values are centralized in the project root gradle.properties.
    // %USERPROFILE%\.gradle\gradle.properties may override them per workstation.
    fun requiredEndpoint(name: String): String = property(name).ifBlank {
        error("Missing required VoiceCloud endpoint Gradle property: $name")
    }
    val googleWebClientId = property("VOICECLOUD_GOOGLE_WEB_CLIENT_ID")
    val firebaseApiKey = property("VOICECLOUD_FIREBASE_API_KEY")
    val firebaseApplicationId = property("VOICECLOUD_FIREBASE_APPLICATION_ID")
    val firebaseProjectId = property("VOICECLOUD_FIREBASE_PROJECT_ID")
    val debugApiUrl = requiredEndpoint("VOICECLOUD_DEBUG_API_BASE_URL")
    val debugSocketUrl = requiredEndpoint("VOICECLOUD_DEBUG_SOCKET_BASE_URL")
    val debugWebUrl = requiredEndpoint("VOICECLOUD_DEBUG_WEB_BASE_URL")
    val stagingApiUrl = property("VOICECLOUD_STAGING_API_BASE_URL").ifBlank { debugApiUrl }
    val stagingSocketUrl = property("VOICECLOUD_STAGING_SOCKET_BASE_URL").ifBlank { debugSocketUrl }
    val stagingWebUrl = property("VOICECLOUD_STAGING_WEB_BASE_URL").ifBlank { debugWebUrl }
    val releaseApiUrl = property("VOICECLOUD_RELEASE_API_BASE_URL").ifBlank { debugApiUrl }
    val releaseSocketUrl = property("VOICECLOUD_RELEASE_SOCKET_BASE_URL").ifBlank { debugSocketUrl }
    val releaseWebUrl = property("VOICECLOUD_RELEASE_WEB_BASE_URL").ifBlank { debugWebUrl }

    defaultConfig.manifestPlaceholders["voicecloudWebHost"] = webHost(debugWebUrl)

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            buildConfigField("String", "API_BASE_URL", quoted(debugApiUrl))
            buildConfigField("String", "SOCKET_BASE_URL", quoted(debugSocketUrl))
            buildConfigField("String", "WEB_BASE_URL", quoted(debugWebUrl))
            buildConfigField("String", "ENVIRONMENT", "\"debug\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", quoted(googleWebClientId))
            buildConfigField("String", "FIREBASE_API_KEY", quoted(firebaseApiKey))
            buildConfigField("String", "FIREBASE_APPLICATION_ID", quoted(firebaseApplicationId))
            buildConfigField("String", "FIREBASE_PROJECT_ID", quoted(firebaseProjectId))
            manifestPlaceholders["voicecloudWebHost"] = webHost(debugWebUrl)
        }
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            matchingFallbacks += listOf("debug")
            buildConfigField("String", "API_BASE_URL", quoted(stagingApiUrl))
            buildConfigField("String", "SOCKET_BASE_URL", quoted(stagingSocketUrl))
            buildConfigField("String", "WEB_BASE_URL", quoted(stagingWebUrl))
            buildConfigField("String", "ENVIRONMENT", "\"staging\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", quoted(googleWebClientId))
            buildConfigField("String", "FIREBASE_API_KEY", quoted(firebaseApiKey))
            buildConfigField("String", "FIREBASE_APPLICATION_ID", quoted(firebaseApplicationId))
            buildConfigField("String", "FIREBASE_PROJECT_ID", quoted(firebaseProjectId))
            manifestPlaceholders["voicecloudWebHost"] = webHost(stagingWebUrl)
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "API_BASE_URL", quoted(releaseApiUrl))
            buildConfigField("String", "SOCKET_BASE_URL", quoted(releaseSocketUrl))
            buildConfigField("String", "WEB_BASE_URL", quoted(releaseWebUrl))
            buildConfigField("String", "ENVIRONMENT", "\"release\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", quoted(googleWebClientId))
            buildConfigField("String", "FIREBASE_API_KEY", quoted(firebaseApiKey))
            buildConfigField("String", "FIREBASE_APPLICATION_ID", quoted(firebaseApplicationId))
            buildConfigField("String", "FIREBASE_PROJECT_ID", quoted(firebaseProjectId))
            manifestPlaceholders["voicecloudWebHost"] = webHost(releaseWebUrl)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
}

ksp { arg("dagger.fastInit", "enabled") }

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:security"))
    implementation(project(":core:database"))
    implementation(project(":core:preferences"))
    implementation(project(":core:logging"))
    implementation(project(":core:realtime"))
    implementation(project(":feature:bootstrap"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:discovery"))
    implementation(project(":feature:engagement"))
    implementation(project(":feature:live"))
    implementation(project(":feature:hosting"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.hilt.android)
    implementation(libs.moshi.kotlin)
    implementation(libs.kotlinx.coroutines.android)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    ksp(libs.hilt.compiler)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
