package app.voicecloud.feature.engagement.data

import org.junit.Assert.assertEquals
import org.junit.Test

class EngagementRouteResolverTest {
    @Test fun conversationRoutesToMessages() = assertEquals("messages/c-1", EngagementRouteResolver.fromData(mapOf("conversationId" to "c-1")))
    @Test fun scheduledRoomRoutesToEvents() = assertEquals("events/e-1", EngagementRouteResolver.fromData(mapOf("scheduledRoomId" to "e-1")))
    @Test fun eventRoutesToEvents() = assertEquals("events/e-2", EngagementRouteResolver.fromData(mapOf("eventId" to "e-2")))
    @Test fun clubRoutesToCommunity() = assertEquals("communities/club-1", EngagementRouteResolver.fromData(mapOf("clubId" to "club-1")))
    @Test fun communityAliasRoutesToCommunity() = assertEquals("communities/club-2", EngagementRouteResolver.fromData(mapOf("communityId" to "club-2")))
    @Test fun roomRoutesToPh05Preview() = assertEquals("rooms/room-1/preview", EngagementRouteResolver.fromData(mapOf("roomId" to "room-1")))
    @Test fun unknownNotificationFallsBackSafely() = assertEquals(EngagementRouteResolver.Notifications, EngagementRouteResolver.fromData(emptyMap<String, String>()))
}
