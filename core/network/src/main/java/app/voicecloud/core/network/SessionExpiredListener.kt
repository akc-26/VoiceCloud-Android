package app.voicecloud.core.network

/** Invoked when refresh/authentication can no longer restore the VoiceCloud session. */
fun interface SessionExpiredListener {
    fun onSessionExpired()
}
