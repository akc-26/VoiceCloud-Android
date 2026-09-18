package app.voicecloud.android.di

import app.voicecloud.android.config.VoiceCloudMobileConfigStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VoiceCloudMobileConfigEntryPoint {
    fun mobileConfigStore(): VoiceCloudMobileConfigStore
}
