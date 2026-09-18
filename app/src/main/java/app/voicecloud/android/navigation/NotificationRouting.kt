package app.voicecloud.android.navigation

import app.voicecloud.core.data.VoiceCloudNotificationItem

sealed class NotificationDestination {
    data class PublicProfile(val userId: String) : NotificationDestination()
    data class Conversation(val conversationId: String) : NotificationDestination()
    data class Room(val roomId: String) : NotificationDestination()
    data class Community(val clubId: String) : NotificationDestination()
    data class Event(val eventId: String) : NotificationDestination()
    data object Notifications : NotificationDestination()
    data object Unknown : NotificationDestination()
}

fun VoiceCloudNotificationItem.resolveDestination(): NotificationDestination {
    conversationId?.takeIf { it.isNotBlank() }?.let { return NotificationDestination.Conversation(it) }
    userId?.takeIf { it.isNotBlank() }?.let { return NotificationDestination.PublicProfile(it) }
    roomId?.takeIf { it.isNotBlank() }?.let { return NotificationDestination.Room(it) }
    clubId?.takeIf { it.isNotBlank() }?.let { return NotificationDestination.Community(it) }
    targetId?.takeIf { it.isNotBlank() }?.let { target ->
        when (targetType?.lowercase()) {
            "user", "profile" -> return NotificationDestination.PublicProfile(target)
            "conversation", "message", "chat" -> return NotificationDestination.Conversation(target)
            "room", "live_room" -> return NotificationDestination.Room(target)
            "club", "community" -> return NotificationDestination.Community(target)
            "event", "scheduled_room" -> return NotificationDestination.Event(target)
        }
        when (type?.lowercase()) {
            "message", "chat" -> return NotificationDestination.Conversation(target)
            "follow", "profile" -> return NotificationDestination.PublicProfile(target)
            "room" -> return NotificationDestination.Room(target)
            "club", "community" -> return NotificationDestination.Community(target)
            "event" -> return NotificationDestination.Event(target)
        }
    }
    return NotificationDestination.Unknown
}
