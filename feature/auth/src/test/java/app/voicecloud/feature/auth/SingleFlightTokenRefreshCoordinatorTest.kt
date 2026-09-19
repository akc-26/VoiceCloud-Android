package app.voicecloud.feature.auth

import app.voicecloud.core.realtime.RealtimeClient
import app.voicecloud.core.realtime.RealtimeConnectionState
import app.voicecloud.core.security.TokenVault
import app.voicecloud.feature.auth.data.PublicAuthApi
import app.voicecloud.feature.auth.data.SingleFlightTokenRefreshCoordinator
import app.voicecloud.feature.auth.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class SingleFlightTokenRefreshCoordinatorTest {
    @Test
    fun concurrent401sRotateRefreshTokenOnlyOnce() {
        val vault = FakeVault("old-access", "old-refresh")
        val api = FakePublicAuthApi()
        val realtime = FakeRealtime()
        val coordinator = SingleFlightTokenRefreshCoordinator(api, vault, realtime)
        val pool = Executors.newFixedThreadPool(2)
        val first = pool.submit<String?> { coordinator.refreshBlocking("old-access") }
        val second = pool.submit<String?> { coordinator.refreshBlocking("old-access") }

        assertEquals("new-access", first.get(3, TimeUnit.SECONDS))
        assertEquals("new-access", second.get(3, TimeUnit.SECONDS))
        assertEquals(1, api.refreshCalls.get())
        assertEquals("new-refresh", vault.refreshToken())
        assertEquals(1, realtime.refreshCount.get())
        pool.shutdownNow()
    }

    @Test
    fun roleParserUsesExactBackendRoleAuthority() {
        assertEquals(VoiceCloudRole.CREATOR, VoiceCloudRole.from("creator"))
        assertEquals(VoiceCloudRole.SUPER_ADMIN, VoiceCloudRole.from("SUPER_ADMIN"))
        assertEquals(VoiceCloudRole.UNKNOWN, VoiceCloudRole.from("host"))
    }
}

private class FakeVault(access: String?, refresh: String?) : TokenVault {
    @Volatile private var accessValue = access
    @Volatile private var refreshValue = refresh
    override fun save(accessToken: String, refreshToken: String) { accessValue = accessToken; refreshValue = refreshToken }
    override fun accessToken(): String? = accessValue
    override fun refreshToken(): String? = refreshValue
    override fun clear() { accessValue = null; refreshValue = null }
}

private class FakeRealtime : RealtimeClient {
    private val mutable = MutableStateFlow<RealtimeConnectionState>(RealtimeConnectionState.Disconnected)
    override val state: StateFlow<RealtimeConnectionState> = mutable
    val refreshCount = AtomicInteger(0)
    override fun connect(): Boolean = true
    override fun refreshAuthentication() { refreshCount.incrementAndGet() }
    override fun disconnect() { mutable.value = RealtimeConnectionState.Disconnected }
    override fun emit(event: String, payload: JSONObject) = Unit
    override fun on(event: String, listener: (Array<out Any>) -> Unit) = Unit
    override fun off(event: String) = Unit
}

private class FakePublicAuthApi : PublicAuthApi {
    val refreshCalls = AtomicInteger(0)
    override suspend fun refresh(request: RefreshTokenRequest): Response<RefreshResponse> {
        refreshCalls.incrementAndGet()
        Thread.sleep(80)
        return Response.success(RefreshResponse("new-access", "new-refresh", expiresIn = 3600))
    }
    override suspend fun login(request: LoginRequest) = unsupported<AuthResponse>()
    override suspend fun register(request: RegisterRequest) = unsupported<AuthResponse>()
    override suspend fun sendPhoneOtp(request: SendOtpRequest) = unsupported<OtpResponse>()
    override suspend fun phoneLogin(request: PhoneLoginRequest) = unsupported<AuthResponse>()
    override suspend fun googleLogin(request: GoogleLoginRequest) = unsupported<AuthResponse>()
    override suspend fun guestLogin(request: GuestLoginRequest) = unsupported<AuthResponse>()
    override suspend fun forgotPassword(request: ForgotPasswordRequest) = unsupported<MessageResponse>()
    override suspend fun resetPassword(request: ResetPasswordRequest) = unsupported<MessageResponse>()
    override suspend fun creatorAccess(request: CreatorAccessApplicationRequest) = unsupported<CreatorAccessApplicationResponse>()
    private fun <T> unsupported(): Response<T> = throw AssertionError("Unexpected API call")
}
