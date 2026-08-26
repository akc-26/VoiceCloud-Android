package app.voicecloud.core.logging

import android.util.Log

interface VoiceCloudLogger {
    fun debug(tag: String, message: String)
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

class AndroidVoiceCloudLogger(private val debugEnabled: Boolean) : VoiceCloudLogger {
    override fun debug(tag: String, message: String) {
        if (debugEnabled) Log.d(safeTag(tag), redact(message))
    }

    override fun warn(tag: String, message: String) {
        Log.w(safeTag(tag), redact(message))
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        // Production logs never include request bodies, authorization values or token material.
        Log.e(safeTag(tag), redact(message), if (debugEnabled) throwable else null)
    }

    private fun safeTag(tag: String): String = "VC-${tag.take(18)}"
    private fun redact(value: String): String = value
        .replace(BEARER, "Bearer [REDACTED]")
        .replace(TOKEN) { match -> "${match.groupValues[1]}=[REDACTED]" }

    private companion object {
        val BEARER = Regex("""(?i)Bearer\s+[A-Za-z0-9._~+/-]+=*""")
        val TOKEN = Regex("""(?i)(access[_-]?token|refresh[_-]?token|authorization|password)=([^\s&]+)""")
    }
}
