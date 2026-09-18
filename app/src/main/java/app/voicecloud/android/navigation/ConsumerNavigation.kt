package app.voicecloud.android.navigation

import android.net.Uri
import androidx.navigation.NavHostController
import app.voicecloud.core.designsystem.component.VCPersonUiModel

fun VCPersonUiModel.profileUserId(): String? =
    userId?.takeIf { it.isNotBlank() }
        ?: subtitle?.removePrefix("@")?.takeIf { it.isNotBlank() }

fun NavHostController.navigateToPublicProfile(userId: String) {
    navigate(
        ConsumerDestinations.PublicProfile.replace(
            "{${ConsumerDestinations.UserIdArg}}",
            Uri.encode(userId),
        ),
    ) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToSocialList(userId: String, listType: String) {
    navigate(
        ConsumerDestinations.SocialList
            .replace("{${ConsumerDestinations.UserIdArg}}", Uri.encode(userId))
            .replace("{${ConsumerDestinations.SocialListTypeArg}}", Uri.encode(listType)),
    ) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToPersonProfile(person: VCPersonUiModel) {
    val id = person.profileUserId() ?: return
    navigateToPublicProfile(id)
}

fun NavHostController.navigateToSessionDetail(sessionId: String) {
    navigate(
        SecurityDestinations.SessionDetail.replace(
            "{${SecurityDestinations.SessionIdArg}}",
            Uri.encode(sessionId),
        ),
    ) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToConversation(conversationId: String, title: String? = null) {
    navigate(
        ConsumerDestinations.MessageThread.replace(
            "{${ConsumerDestinations.ConversationIdArg}}",
            Uri.encode(conversationId),
        ),
    ) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToCommunity(clubId: String) {
    navigate(
        ConsumerDestinations.CommunityDetail.replace(
            "{${ConsumerDestinations.ClubIdArg}}",
            Uri.encode(clubId),
        ),
    ) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToEvent(eventId: String) {
    navigate(
        ConsumerDestinations.EventDetail.replace(
            "{${ConsumerDestinations.EventIdArg}}",
            Uri.encode(eventId),
        ),
    ) {
        launchSingleTop = true
    }
}

fun NavHostController.handleNotificationDestination(destination: NotificationDestination) {
    when (destination) {
        is NotificationDestination.PublicProfile -> navigateToPublicProfile(destination.userId)
        is NotificationDestination.Conversation -> navigateToConversation(destination.conversationId)
        is NotificationDestination.Room -> {
            // Room preview requires UI state; navigate to live/discover entry
            navigate(ConsumerDestinations.Discover) { launchSingleTop = true }
        }
        is NotificationDestination.Community -> navigateToCommunity(destination.clubId)
        is NotificationDestination.Event -> navigateToEvent(destination.eventId)
        NotificationDestination.Notifications -> navigate(ConsumerDestinations.Notifications) { launchSingleTop = true }
        NotificationDestination.Unknown -> Unit
    }
}

fun NavHostController.navigateToDeviceDetail(deviceId: String) {
    navigate(
        SecurityDestinations.DeviceDetail.replace(
            "{${SecurityDestinations.DeviceIdArg}}",
            Uri.encode(deviceId),
        ),
    ) {
        launchSingleTop = true
    }
}

fun String?.hidesConsumerBottomBar(): Boolean {
    if (this == null) return false
    return this == ConsumerDestinations.RoomPreview ||
        this == ConsumerDestinations.MessageThread ||
        this == ConsumerDestinations.Wallet ||
        this == ConsumerDestinations.Settings ||
        this == ConsumerDestinations.People ||
        this == ConsumerDestinations.Notifications ||
        this == ConsumerDestinations.EditProfile ||
        this == ConsumerDestinations.Search ||
        this == ConsumerDestinations.SavedRooms ||
        this == ConsumerDestinations.Rankings ||
        this == ConsumerDestinations.BlockedUsers ||
        this == ConsumerDestinations.Referrals ||
        this == ConsumerDestinations.TasksHub ||
        this == ConsumerDestinations.Friends ||
        this == ConsumerDestinations.ProfileVisitors ||
        this == ConsumerDestinations.ActivityHistory ||
        this == ConsumerDestinations.NotificationPreferences ||
        this == ConsumerDestinations.Communities ||
        this == ConsumerDestinations.CreateCommunity ||
        this == ConsumerDestinations.Events ||
        startsWith("user/communities/") ||
        startsWith("user/events/") ||
        startsWith("user/messages/thread/") ||
        this == SecurityDestinations.Sessions ||
        this == SecurityDestinations.LoginHistory ||
        this == SecurityDestinations.Devices ||
        startsWith("user/profile/user/") ||
        startsWith("user/security/sessions/") ||
        startsWith("user/security/devices/")
}
