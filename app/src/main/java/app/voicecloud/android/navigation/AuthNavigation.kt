package app.voicecloud.android.navigation

import androidx.navigation.NavHostController
import app.voicecloud.core.model.VoiceCloudAccountRole
import app.voicecloud.core.model.VoiceCloudUser
import app.voicecloud.core.preferences.LastPortal
import app.voicecloud.core.preferences.VoiceCloudPreferences
import app.voicecloud.feature.auth.AuthSessionController
import kotlinx.coroutines.flow.first

suspend fun NavHostController.navigateAfterAuthentication(
    user: VoiceCloudUser,
    preferences: VoiceCloudPreferences,
    sessionController: AuthSessionController,
    openPortalSelection: () -> Unit,
    openUserPortal: () -> Unit,
    openCreatorPortal: () -> Unit,
) {
    when {
        user.isRestricted || user.isSuspended -> {
            navigate(AuthDestinations.RestrictedAccount) {
                launchSingleTop = true
            }
        }
        !preferences.onboardingComplete.first() -> {
            navigate(AuthDestinations.Onboarding) { launchSingleTop = true }
        }
        user.accountRole == VoiceCloudAccountRole.GUEST -> openUserPortal()
        user.accountRole.canAccessCreatorPortal() -> {
            when (preferences.lastPortal.first()) {
                LastPortal.CREATOR -> openCreatorPortal()
                else -> openPortalSelection()
            }
        }
        else -> {
            sessionController.persistPortal(LastPortal.USER)
            openUserPortal()
        }
    }
}
