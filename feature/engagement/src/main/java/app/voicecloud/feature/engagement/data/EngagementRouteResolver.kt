package app.voicecloud.feature.engagement.data

import app.voicecloud.feature.engagement.model.VoiceCloudNotification

object EngagementRouteResolver {
    const val Notifications = "notifications"
    const val Communities = "communities"
    const val Events = "events"
    const val Messages = "messages"
    const val Rooms = "rooms"

    fun fromNotification(item: VoiceCloudNotification): String = fromData(item.data.orEmpty())

    fun fromStringData(data: Map<String, String>): String = fromData(data)

    fun fromData(data: Map<String, *>): String {
        fun string(key: String): String? = data[key]?.toString()?.trim()?.takeIf { it.isNotBlank() && it != "null" }
        return when {
            string("conversationId") != null -> "messages/${string("conversationId")}"
            string("scheduledRoomId") != null -> "events/${string("scheduledRoomId")}"
            string("eventId") != null -> "events/${string("eventId")}"
            string("clubId") != null -> "communities/${string("clubId")}"
            string("communityId") != null -> "communities/${string("communityId")}"
            string("roomId") != null -> "rooms/${string("roomId")}/preview"
            else -> Notifications
        }
    }
}
