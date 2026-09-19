package app.voicecloud.feature.auth.data

import app.voicecloud.core.network.TokenRefreshCoordinator
import app.voicecloud.core.realtime.RealtimeClient
import app.voicecloud.core.security.TokenVault
import app.voicecloud.feature.auth.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/** Serializes all concurrent 401 refresh attempts and rotates both tokens atomically in TokenVault. */
@Singleton
class SingleFlightTokenRefreshCoordinator @Inject constructor(
    private val publicApi: PublicAuthApi,
    private val tokenVault: TokenVault,
    private val realtimeClient: RealtimeClient,
) : TokenRefreshCoordinator {
    private val lock = Any()

    override fun refreshBlocking(failedAccessToken: String?): String? = synchronized(lock) {
        val current = tokenVault.accessToken()?.takeIf { it.isNotBlank() }
        if (current != null && failedAccessToken != null && current != failedAccessToken) return@synchronized current

        val refreshToken = tokenVault.refreshToken()?.takeIf { it.isNotBlank() } ?: return@synchronized null
        val response = runCatching {
            runBlocking { publicApi.refresh(RefreshTokenRequest(refreshToken)) }
        }.getOrNull() ?: return@synchronized null

        val body = response.body()
        if (!response.isSuccessful || body == null || body.accessToken.isBlank() || body.refreshToken.isBlank()) {
            tokenVault.clear()
            realtimeClient.disconnect()
            return@synchronized null
        }
        tokenVault.save(body.accessToken, body.refreshToken)
        realtimeClient.refreshAuthentication()
        body.accessToken
    }
}
