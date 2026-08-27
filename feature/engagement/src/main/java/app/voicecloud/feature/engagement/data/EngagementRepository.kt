package app.voicecloud.feature.engagement.data

import app.voicecloud.feature.discovery.model.VoiceCloudUser
import app.voicecloud.feature.engagement.model.*
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EngagementRepository @Inject constructor(
    private val api: EngagementApi,
    private val realtimeMonitor: NotificationRealtimeMonitor,
) {
    suspend fun communities(search: String = "", category: String = ""): List<Community> =
        api.communities(search.trim().ifBlank { null }, category.trim().ifBlank { null }).data.distinctBy { it.id }

    suspend fun community(id: String): Community = api.community(id.trim())

    suspend fun createCommunity(input: CommunityInput): Community = api.createCommunity(input.normalized())

    suspend fun updateCommunity(id: String, input: CommunityInput): Community = api.updateCommunity(id, input.normalized())

    suspend fun deleteCommunity(id: String) = api.deleteCommunity(id)

    suspend fun membership(id: String): CommunityMembership = api.membership(id)

    suspend fun joinCommunity(id: String, inviteCode: String?): CommunityMember =
        api.joinCommunity(id, JoinCommunityBody(inviteCode?.trim()?.ifBlank { null }))

    suspend fun leaveCommunity(id: String) = api.leaveCommunity(id)

    suspend fun members(id: String, search: String = ""): List<CommunityMember> =
        api.communityMembers(id, search = search.trim().ifBlank { null }).data
            .filter { it.user == null || isConsumerIdentity(it.user) }
            .distinctBy { it.id.ifBlank { "${it.clubId}:${it.userId}" } }

    suspend fun communityEvents(id: String): List<ScheduledEvent> =
        api.communityEvents(id).data.distinctBy { it.id }

    suspend fun rotateInviteCode(id: String): InviteCodeResult = api.rotateInviteCode(id)

    suspend fun updateMemberRole(id: String, userId: String, role: String): CommunityMember {
        require(role.uppercase() in setOf("OWNER", "ADMIN", "MODERATOR", "MEMBER")) { "Invalid community role." }
        return api.updateCommunityMemberRole(id, userId, UpdateCommunityRoleBody(role.uppercase()))
    }

    suspend fun removeMember(id: String, userId: String) = api.removeCommunityMember(id, userId)

    suspend fun events(search: String = ""): List<ScheduledEvent> =
        api.events(search.trim().ifBlank { null }).data.distinctBy { it.id }

    suspend fun event(id: String): ScheduledEvent = api.event(id)

    suspend fun reminder(id: String): ReminderResult = api.registerReminder(id)

    suspend fun conversations(search: String = ""): ConversationList {
        val result = api.conversations(search = search.trim().ifBlank { null })
        return result.copy(conversations = result.conversations.distinctBy { it.id })
    }

    suspend fun conversation(id: String): Conversation = api.conversation(id)

    suspend fun messages(id: String): MessagePage {
        val result = api.messages(id)
        return result.copy(messages = result.messages.distinctBy { it.id })
    }

    suspend fun sendMessage(id: String, content: String): ChatMessage {
        val clean = content.trim()
        require(clean.isNotBlank()) { "Message cannot be empty." }
        return api.sendMessage(id, SendMessageBody(content = clean))
    }

    suspend fun markConversationRead(id: String, lastReadMessageId: String?) =
        api.markConversationRead(id, ReadConversationBody(lastReadMessageId))

    suspend fun directConversation(recipientId: String): Conversation =
        api.createConversation(CreateConversationBody(type = "direct", recipientId = recipientId))

    suspend fun deleteConversation(id: String) = api.deleteConversation(id)

    suspend fun notifications(): Page<VoiceCloudNotification> = api.notifications()
    suspend fun unreadCount(): Int = api.unreadCount().unreadCount
    suspend fun markNotificationRead(id: String) = api.markNotificationRead(id)
    suspend fun markAllNotificationsRead() = api.markAllNotificationsRead()
    suspend fun deleteNotification(id: String) = api.deleteNotification(id)

    val notificationChanges: SharedFlow<Unit> get() = realtimeMonitor.changes
    val notificationRealtimeState get() = realtimeMonitor.state
    fun startNotificationRealtime() = realtimeMonitor.start()
    fun ensureNotificationRealtimeListeners() = realtimeMonitor.ensureListeners()
    fun stopNotificationRealtime() = realtimeMonitor.stop()

    private fun CommunityInput.normalized(): CommunityInput {
        val cleanName = name.trim()
        val cleanHandle = handle.trim().lowercase().replace(Regex("[^a-z0-9_-]"), "-").trim('-')
        require(cleanName.length >= 2) { "Community name is required." }
        require(cleanHandle.length >= 2) { "Community handle is required." }
        val normalizedVisibility = visibility?.uppercase()?.takeIf { it in setOf("PUBLIC", "PRIVATE") } ?: "PUBLIC"
        return copy(
            name = cleanName,
            handle = cleanHandle,
            description = description?.trim(),
            category = category?.trim()?.ifBlank { "General" },
            rules = rules?.map(String::trim)?.filter(String::isNotBlank),
            visibility = normalizedVisibility,
        )
    }

    private fun isConsumerIdentity(user: VoiceCloudUser): Boolean {
        val role = user.role?.trim()?.uppercase()
        return !user.isGuest && role in setOf("USER", "CREATOR")
    }
}
