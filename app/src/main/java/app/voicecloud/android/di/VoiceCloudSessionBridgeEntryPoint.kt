package app.voicecloud.android.di

import app.voicecloud.android.session.VoiceCloudSessionNavigationBridge
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VoiceCloudSessionBridgeEntryPoint {
    fun sessionNavigationBridge(): VoiceCloudSessionNavigationBridge
}
