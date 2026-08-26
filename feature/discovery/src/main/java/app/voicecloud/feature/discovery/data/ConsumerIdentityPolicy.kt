package app.voicecloud.feature.discovery.data

import app.voicecloud.feature.discovery.model.ViewerIdentity
import app.voicecloud.feature.discovery.model.VoiceCloudUser

/**
 * Consumer identity authority shared by Home/Explore/Search/Social/Friends.
 * Administrative identities and guests are never rendered as ordinary people; discovery also excludes self.
 */
object ConsumerIdentityPolicy {
    private val consumerRoles = setOf("USER", "CREATOR")

    fun isVisible(user: VoiceCloudUser): Boolean {
        if (user.isGuest) return false
        val role = user.role?.trim()?.uppercase()
        return role == null || role in consumerRoles
    }

    fun isDiscoverable(user: VoiceCloudUser, viewer: ViewerIdentity): Boolean {
        if (!isVisible(user)) return false
        if (!viewer.id.isNullOrBlank() && user.id == viewer.id) return false
        val viewerName = viewer.username?.trim()?.lowercase()
        if (!viewerName.isNullOrBlank() && user.username.trim().lowercase() == viewerName) return false
        return true
    }

    fun filter(users: List<VoiceCloudUser>, viewer: ViewerIdentity): List<VoiceCloudUser> =
        users
            .filter { isDiscoverable(it, viewer) }
            .distinctBy { it.id.ifBlank { it.username.trim().lowercase() } }
}
