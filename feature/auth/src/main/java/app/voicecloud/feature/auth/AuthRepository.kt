package app.voicecloud.feature.auth

import app.voicecloud.core.model.ApiError
import app.voicecloud.core.model.AuthTokenBundle
import app.voicecloud.core.model.LoginRequest
import app.voicecloud.core.model.LogoutRequest
import app.voicecloud.core.model.RegisterRequest
import app.voicecloud.core.model.VoiceCloudUser
import app.voicecloud.core.network.AuthApi
import app.voicecloud.core.network.SafeApiErrorParser
import app.voicecloud.core.security.TokenVault

sealed interface AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>
    data class Failure(val error: ApiError) : AuthResult<Nothing>
}

class AuthRepository(
    private val api: AuthApi,
    private val tokenVault: TokenVault,
    private val errorParser: SafeApiErrorParser,
) {
    suspend fun login(identifier: String, password: String, useEmail: Boolean): AuthResult<AuthTokenBundle> {
        val body = if (useEmail) {
            LoginRequest(email = identifier, password = password)
        } else {
            LoginRequest(username = identifier, password = password)
        }
        return tokenCall { api.login(body) }
    }

    suspend fun register(
        email: String,
        username: String,
        password: String,
        displayName: String?,
    ): AuthResult<AuthTokenBundle> = tokenCall {
        api.register(RegisterRequest(email, username, password, displayName))
    }

    suspend fun restoreSession(): AuthResult<VoiceCloudUser> {
        if (tokenVault.accessToken().isNullOrBlank()) {
            return AuthResult.Failure(ApiError(message = "No saved session", retryable = false))
        }
        return try {
            val response = api.me()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AuthResult.Success(body)
            } else {
                if (response.code() == 401) tokenVault.clear()
                AuthResult.Failure(errorParser.parse(response))
            }
        } catch (_: Throwable) {
            AuthResult.Failure(ApiError(message = "Could not restore your session.", retryable = true))
        }
    }

    suspend fun logout(): AuthResult<Unit> = try {
        val response = api.logout(LogoutRequest(refreshToken = tokenVault.refreshToken()))
        tokenVault.clear()
        if (response.isSuccessful) AuthResult.Success(Unit) else AuthResult.Failure(errorParser.parse(response))
    } catch (_: Throwable) {
        tokenVault.clear()
        AuthResult.Success(Unit)
    }

    suspend fun logoutAll(): AuthResult<Unit> = try {
        val response = api.logoutAll()
        tokenVault.clear()
        if (response.isSuccessful) AuthResult.Success(Unit) else AuthResult.Failure(errorParser.parse(response))
    } catch (_: Throwable) {
        tokenVault.clear()
        AuthResult.Success(Unit)
    }

    suspend fun guestLogin(): AuthResult<AuthTokenBundle> = tokenCall { api.guestLogin() }

    suspend fun sendOtp(phone: String): AuthResult<Unit> = try {
        val response = api.sendOtp(app.voicecloud.core.model.PhoneSendOtpRequest(phone))
        if (response.isSuccessful) AuthResult.Success(Unit) else AuthResult.Failure(errorParser.parse(response))
    } catch (_: Throwable) {
        AuthResult.Failure(ApiError(message = "Could not send verification code.", retryable = true))
    }

    suspend fun phoneLogin(phone: String, code: String): AuthResult<AuthTokenBundle> =
        tokenCall { api.phoneLogin(app.voicecloud.core.model.PhoneLoginRequest(phone, code)) }

    suspend fun forgotPassword(email: String): AuthResult<Unit> = try {
        val response = api.forgotPassword(app.voicecloud.core.model.ForgotPasswordRequest(email))
        if (response.isSuccessful) AuthResult.Success(Unit) else AuthResult.Failure(errorParser.parse(response))
    } catch (_: Throwable) {
        AuthResult.Failure(ApiError(message = "Could not send reset instructions.", retryable = true))
    }

    suspend fun resetPassword(token: String, password: String): AuthResult<Unit> = try {
        val response = api.resetPassword(app.voicecloud.core.model.ResetPasswordRequest(token, password))
        if (response.isSuccessful) AuthResult.Success(Unit) else AuthResult.Failure(errorParser.parse(response))
    } catch (_: Throwable) {
        AuthResult.Failure(ApiError(message = "Could not reset password.", retryable = true))
    }

    suspend fun googleLogin(idToken: String): AuthResult<AuthTokenBundle> =
        tokenCall { api.googleLogin(app.voicecloud.core.model.GoogleLoginRequest(idToken)) }

    suspend fun upgradeGuest(
        email: String,
        username: String,
        password: String,
    ): AuthResult<AuthTokenBundle> = tokenCall {
        api.guestUpgrade(
            app.voicecloud.core.model.GuestUpgradeRequest(
                email = email,
                username = username,
                password = password,
            ),
        )
    }

    private suspend inline fun <reified T> tokenCall(crossinline call: suspend () -> retrofit2.Response<T>): AuthResult<T> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (body is AuthTokenBundle) {
                    tokenVault.save(body.accessToken, body.refreshToken)
                }
                AuthResult.Success(body)
            } else {
                AuthResult.Failure(errorParser.parse(response))
            }
        } catch (_: Throwable) {
            AuthResult.Failure(ApiError(message = "VoiceCloud authentication is unreachable.", retryable = true))
        }
    }
}
