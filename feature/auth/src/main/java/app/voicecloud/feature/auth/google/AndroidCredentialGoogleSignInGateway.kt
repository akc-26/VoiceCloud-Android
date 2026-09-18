package app.voicecloud.feature.auth.google

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import app.voicecloud.core.model.GoogleSignInConfig
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidCredentialGoogleSignInGateway @Inject constructor(
    private val config: GoogleSignInConfig,
) : GoogleSignInGateway {

    override suspend fun signIn(context: Context): GoogleSignInOutcome {
        if (!config.isConfigured) {
            return GoogleSignInOutcome.Failure(
                message = "Google Sign-In is not configured for this build. Set VOICECLOUD_GOOGLE_WEB_CLIENT_ID in gradle.properties.",
                retryable = false,
            )
        }
        val availability = GoogleApiAvailability.getInstance()
        val servicesResult = availability.isGooglePlayServicesAvailable(context)
        if (servicesResult != ConnectionResult.SUCCESS) {
            val recoverable = availability.isUserResolvableError(servicesResult)
            return GoogleSignInOutcome.Failure(
                message = if (recoverable) {
                    "Google Play services must be updated before you can sign in with Google."
                } else {
                    "Google Play services are unavailable on this device."
                },
                retryable = recoverable,
            )
        }
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(config.webClientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val token = googleCredential.idToken.trim()
                if (token.isEmpty()) {
                    GoogleSignInOutcome.Failure("Google did not return a valid sign-in token.", retryable = true)
                } else {
                    GoogleSignInOutcome.Success(token)
                }
            } else {
                GoogleSignInOutcome.Failure("Unexpected Google credential type.", retryable = true)
            }
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInOutcome.Cancelled
        } catch (_: NoCredentialException) {
            GoogleSignInOutcome.Failure("No Google account is available for sign-in.", retryable = true)
        } catch (error: GetCredentialException) {
            GoogleSignInOutcome.Failure(
                message = error.message?.takeIf { it.isNotBlank() }
                    ?: "Google Sign-In could not complete.",
                retryable = true,
            )
        } catch (_: Throwable) {
            GoogleSignInOutcome.Failure("Google Sign-In could not reach Google services.", retryable = true)
        }
    }
}
