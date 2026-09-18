package app.voicecloud.android.config

import app.voicecloud.core.model.MobileConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceCloudMobileConfigStore @Inject constructor() {
    @Volatile
    var config: MobileConfig? = null
        private set

    fun update(value: MobileConfig) {
        config = value
    }
}
