import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

val brandProperties = Properties().apply {
    rootProject.file("branding/voicecloud-brand.properties").inputStream().use { load(it) }
}
fun brand(key: String): String = brandProperties.getProperty(key)?.trim().orEmpty().ifBlank {
    error("Missing white-label branding property: $key")
}
fun quotedBrand(key: String): String = "\"${brand(key).replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "app.voicecloud.core.designsystem"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    androidResources {
        enable = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    defaultConfig {
        val keys = listOf(
            "brand.name", "brand.slug", "brand.legalName", "brand.tagline", "brand.websiteTagline",
            "consumer.sapphire", "consumer.sapphireDeep", "consumer.sapphireSoft", "consumer.ice", "consumer.indigo",
            "consumer.violet", "consumer.violetDeep", "consumer.lavender", "consumer.lavenderStrong", "consumer.sky",
            "consumer.cloud", "consumer.surface", "consumer.surfaceSoft", "consumer.ink", "consumer.text", "consumer.textMuted",
            "consumer.border", "consumer.deepNavy", "consumer.navy", "consumer.liveSurface", "consumer.liveSurfaceElevated",
            "consumer.textOnDark", "consumer.textOnDarkSecondary", "consumer.vipGold", "consumer.primaryGradientStart",
            "consumer.primaryGradientEnd", "consumer.heroGradientStart", "consumer.heroGradientMiddle", "consumer.heroGradientEnd",
            "consumer.darkSapphire", "consumer.darkSapphireDeep", "consumer.darkSurface", "consumer.darkSurfaceSoft",
            "consumer.darkBackground", "consumer.darkText", "consumer.darkMuted", "consumer.darkBorder",
            "creator.primary", "creator.primaryLight", "creator.primaryDark", "creator.secondary", "creator.secondaryLight",
            "creator.accent", "creator.lightBackground", "creator.lightSurface", "creator.elevated", "creator.navigation",
            "creator.text", "creator.textMuted", "creator.border", "creator.darkBackground", "creator.darkSurface",
            "creator.darkElevated", "creator.darkText", "creator.darkMuted", "creator.darkBorder",
            "common.success", "common.warning", "common.error", "common.info", "common.logo"
        )
        keys.forEach { key ->
            val field = key.uppercase().replace('.', '_')
            buildConfigField("String", field, quotedBrand(key))
        }
        buildConfigField("int", "RADIUS_SMALL", brand("radius.small"))
        buildConfigField("int", "RADIUS_MEDIUM", brand("radius.medium"))
        buildConfigField("int", "RADIUS_LARGE", brand("radius.large"))
        buildConfigField("int", "RADIUS_EXTRA_LARGE", brand("radius.extraLarge"))
        buildConfigField("int", "MOTION_FAST_MS", brand("motion.fastMs"))
        buildConfigField("int", "MOTION_STANDARD_MS", brand("motion.standardMs"))
        buildConfigField("int", "MOTION_SLOW_MS", brand("motion.slowMs"))
        resValue("color", "vc_brand_primary", brand("consumer.sapphire"))
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

androidComponents {
    onVariants(selector().all()) { variant ->
        variant.sources.res?.addStaticSourceDirectory("../../branding/res")
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    api(composeBom)
    api(libs.androidx.compose.ui)

    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
