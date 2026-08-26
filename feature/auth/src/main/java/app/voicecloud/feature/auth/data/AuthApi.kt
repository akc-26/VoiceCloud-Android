package app.voicecloud.feature.auth.data

import app.voicecloud.feature.auth.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface PublicAuthApi {
    @POST("auth/login") suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    @POST("auth/register") suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    @POST("auth/phone/send-otp") suspend fun sendPhoneOtp(@Body request: SendOtpRequest): Response<OtpResponse>
    @POST("auth/phone/login") suspend fun phoneLogin(@Body request: PhoneLoginRequest): Response<AuthResponse>
    @POST("auth/google/login") suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>
    @POST("auth/guest/login") suspend fun guestLogin(@Body request: GuestLoginRequest): Response<AuthResponse>
    @POST("auth/forgot-password") suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>
    @POST("auth/reset-password") suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>
    @POST("auth/refresh") suspend fun refresh(@Body request: RefreshTokenRequest): Response<RefreshResponse>
    @POST("creator-access/applications") suspend fun creatorAccess(@Body request: CreatorAccessApplicationRequest): Response<CreatorAccessApplicationResponse>
}

interface AuthenticatedAuthApi {
    @GET("auth/me") suspend fun me(): Response<AuthUser>
    @POST("auth/guest/upgrade") suspend fun upgradeGuest(@Body request: GuestUpgradeRequest): Response<AuthResponse>
    @POST("auth/logout") suspend fun logout(): Response<MessageResponse>
    @POST("auth/logout-all") suspend fun logoutAll(): Response<MessageResponse>
    @PATCH("users/profile") suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiMutationResponse>
    @PATCH("users/settings") suspend fun updateSettings(@Body request: UpdateSettingsRequest): Response<ApiMutationResponse>
    @POST("notifications/register-device") suspend fun registerDevice(@Body request: RegisterDeviceRequest): Response<ApiMutationResponse>
}
