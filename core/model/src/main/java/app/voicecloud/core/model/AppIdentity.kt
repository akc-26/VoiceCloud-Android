package app.voicecloud.core.model

data class AppIdentity(
    val versionName: String,
    val environment: String,
)


enum class PortalIdentity { USER, CREATOR }
