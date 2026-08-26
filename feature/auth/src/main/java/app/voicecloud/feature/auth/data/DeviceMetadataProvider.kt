package app.voicecloud.feature.auth.data

import android.content.Context
import android.os.Build
import android.provider.Settings
import app.voicecloud.core.model.AppIdentity
import app.voicecloud.feature.auth.model.DeviceMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceMetadataProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appIdentity: AppIdentity,
) {
    fun current(): DeviceMetadata {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
        val stableId = "android_${sha256(androidId.ifBlank { "voicecloud-unknown-device" }).take(24)}"
        val manufacturer = Build.MANUFACTURER.orEmpty().ifBlank { "Android" }
        val model = Build.MODEL.orEmpty().ifBlank { "Device" }
        return DeviceMetadata(
            deviceId = stableId,
            deviceName = listOf(manufacturer, model).filter { it.isNotBlank() }.distinct().joinToString(" "),
            deviceType = "mobile",
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            appVersion = appIdentity.versionName,
            manufacturer = manufacturer,
            model = model,
        )
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
