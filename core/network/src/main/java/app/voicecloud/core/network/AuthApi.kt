package app.voicecloud.core.network

import app.voicecloud.core.model.AuthTokenBundle
import app.voicecloud.core.model.ForgotPasswordRequest
import app.voicecloud.core.model.GoogleLoginRequest
import app.voicecloud.core.model.GuestLoginRequest
import app.voicecloud.core.model.GuestUpgradeRequest
import app.voicecloud.core.model.LoginRequest
import app.voicecloud.core.model.LogoutRequest
import app.voicecloud.core.model.PhoneLoginRequest
import app.voicecloud.core.model.PhoneSendOtpRequest
import app.voicecloud.core.model.RefreshTokenRequest
import app.voicecloud.core.model.RefreshTokenResponse
import app.voicecloud.core.model.RegisterRequest
import app.voicecloud.core.model.ResetPasswordRequest
import app.voicecloud.core.model.VoiceCloudUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthTokenBundle>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthTokenBundle>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("auth/me")
    suspend fun me(): Response<VoiceCloudUser>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest = LogoutRequest()): Response<Unit>

    @POST("auth/logout-all")
    suspend fun logoutAll(): Response<Unit>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<Unit>

    @POST("auth/phone/send-otp")
    suspend fun sendOtp(@Body body: PhoneSendOtpRequest): Response<Unit>

    @POST("auth/phone/login")
    suspend fun phoneLogin(@Body body: PhoneLoginRequest): Response<AuthTokenBundle>

    @POST("auth/google/login")
    suspend fun googleLogin(@Body body: GoogleLoginRequest): Response<AuthTokenBundle>

    @POST("auth/guest/login")
    suspend fun guestLogin(@Body body: GuestLoginRequest = GuestLoginRequest()): Response<AuthTokenBundle>

    @POST("auth/guest/upgrade")
    suspend fun guestUpgrade(@Body body: GuestUpgradeRequest): Response<AuthTokenBundle>
}
