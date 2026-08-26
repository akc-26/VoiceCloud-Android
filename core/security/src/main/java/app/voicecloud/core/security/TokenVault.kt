package app.voicecloud.core.security

interface TokenVault {
    fun save(accessToken: String, refreshToken: String)
    fun accessToken(): String?
    fun refreshToken(): String?
    fun clear()
}
