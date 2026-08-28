package app.voicecloud.feature.engagement.ui

import app.voicecloud.core.network.toVoiceCloudUserMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.core.realtime.RealtimeConnectionState
import app.voicecloud.feature.engagement.data.EngagementRepository
import app.voicecloud.feature.engagement.data.PushNotificationCoordinator
import app.voicecloud.feature.engagement.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EngagementUiState(
    val loading: Boolean = false,
    val mutationBusy: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
    val communities: List<Community> = emptyList(),
    val community: Community? = null,
    val membership: CommunityMembership? = null,
    val members: List<CommunityMember> = emptyList(),
    val communityEvents: List<ScheduledEvent> = emptyList(),
    val inviteCode: String? = null,
    val events: List<ScheduledEvent> = emptyList(),
    val event: ScheduledEvent? = null,
    val conversations: List<Conversation> = emptyList(),
    val conversation: Conversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val notifications: List<VoiceCloudNotification> = emptyList(),
    val unreadNotifications: Int = 0,
)

@HiltViewModel
class EngagementViewModel @Inject constructor(
    private val repository: EngagementRepository,
    private val pushCoordinator: PushNotificationCoordinator,
) : ViewModel() {
    private val _state = MutableStateFlow(EngagementUiState())
    val state: StateFlow<EngagementUiState> = _state.asStateFlow()
    private var notificationJob: Job? = null

    fun clearFeedback() { _state.value = _state.value.copy(error = null, notice = null) }

    fun syncPushToken() {
        viewModelScope.launch { runCatching { pushCoordinator.syncCurrentToken() } }
    }

    fun loadCommunities(search: String = "") = load {
        _state.value = _state.value.copy(communities = repository.communities(search))
    }

    fun loadCommunity(id: String) = load {
        val community = repository.community(id)
        val membership = runCatching { repository.membership(community.id) }.getOrNull()
        val mayReadPrivate = community.visibility.uppercase() != "PRIVATE" || membership?.member == true
        _state.value = _state.value.copy(
            community = community,
            membership = membership,
            members = if (mayReadPrivate) runCatching { repository.members(community.id) }.getOrDefault(emptyList()) else emptyList(),
            communityEvents = if (mayReadPrivate) runCatching { repository.communityEvents(community.id) }.getOrDefault(emptyList()) else emptyList(),
            inviteCode = if (_state.value.community?.id == community.id) _state.value.inviteCode ?: community.inviteCode else community.inviteCode,
        )
    }

    fun createCommunity(input: CommunityInput, onCreated: (Community) -> Unit) = mutate("Community created.") {
        val created = repository.createCommunity(input)
        _state.value = _state.value.copy(community = created, inviteCode = created.inviteCode)
        onCreated(created)
    }

    fun updateCommunity(input: CommunityInput) {
        val current = _state.value.community ?: return
        mutate("Community updated.") {
            val updated = repository.updateCommunity(current.id, input)
            _state.value = _state.value.copy(community = updated, inviteCode = updated.inviteCode ?: _state.value.inviteCode)
        }
    }


    fun deleteCommunity(onDeleted: () -> Unit) {
        val current = _state.value.community ?: return
        mutate("Community deleted.") {
            repository.deleteCommunity(current.id)
            _state.value = _state.value.copy(community = null, membership = null, members = emptyList(), communityEvents = emptyList(), inviteCode = null)
            onDeleted()
        }
    }

    fun joinCommunity(inviteCode: String? = null) {
        val current = _state.value.community ?: return
        mutate("Joined community.") {
            repository.joinCommunity(current.id, inviteCode)
            refreshCommunity(current.id)
        }
    }

    fun leaveCommunity() {
        val current = _state.value.community ?: return
        mutate("Left community.") {
            repository.leaveCommunity(current.id)
            refreshCommunity(current.id)
        }
    }

    fun rotateInviteCode() {
        val current = _state.value.community ?: return
        mutate("Private invitation code rotated.") {
            val result = repository.rotateInviteCode(current.id)
            _state.value = _state.value.copy(inviteCode = result.inviteCode)
        }
    }

    fun updateMemberRole(userId: String, role: String) {
        val current = _state.value.community ?: return
        mutate("Community role updated.") {
            repository.updateMemberRole(current.id, userId, role)
            _state.value = _state.value.copy(members = repository.members(current.id))
        }
    }

    fun removeMember(userId: String) {
        val current = _state.value.community ?: return
        mutate("Member removed.") {
            repository.removeMember(current.id, userId)
            refreshCommunity(current.id)
        }
    }

    fun loadEvents(search: String = "") = load {
        _state.value = _state.value.copy(events = repository.events(search))
    }

    fun loadEvent(id: String) = load {
        _state.value = _state.value.copy(event = repository.event(id))
    }

    fun remindEvent() {
        val current = _state.value.event ?: return
        mutate("Reminder registered.") {
            val result = repository.reminder(current.id)
            _state.value = _state.value.copy(event = current.copy(rsvpCount = result.rsvpCount))
        }
    }

    fun loadConversations(search: String = "") = load {
        _state.value = _state.value.copy(conversations = repository.conversations(search).conversations)
    }

    fun loadConversation(id: String) = load {
        val conversation = repository.conversation(id)
        val messages = repository.messages(id).messages
        _state.value = _state.value.copy(conversation = conversation, messages = messages)
        repository.markConversationRead(id, messages.lastOrNull()?.id)
    }


    fun refreshConversationSilently(id: String) {
        viewModelScope.launch {
            runCatching {
                val conversation = repository.conversation(id)
                val messages = repository.messages(id).messages
                _state.value = _state.value.copy(conversation = conversation, messages = messages)
                repository.markConversationRead(id, messages.lastOrNull()?.id)
            }
        }
    }

    fun sendMessage(content: String) {
        val current = _state.value.conversation ?: return
        mutate("Message sent.", keepNotice = false) {
            repository.sendMessage(current.id, content)
            val messages = repository.messages(current.id).messages
            _state.value = _state.value.copy(messages = messages)
            repository.markConversationRead(current.id, messages.lastOrNull()?.id)
        }
    }

    fun deleteConversation(id: String) = mutate("Conversation removed.") {
        repository.deleteConversation(id)
        _state.value = _state.value.copy(conversations = repository.conversations().conversations)
    }

    fun deleteConversations(ids: Set<String>) {
        val clean = ids.filter(String::isNotBlank).toSet()
        if (clean.isEmpty()) return
        mutate(if (clean.size == 1) "Conversation removed." else "Conversations removed.") {
            clean.forEach { repository.deleteConversation(it) }
            _state.value = _state.value.copy(conversations = repository.conversations().conversations)
        }
    }

    fun startDirectConversation(recipientId: String, onReady: (Conversation) -> Unit) = mutate("Conversation ready.", keepNotice = false) {
        onReady(repository.directConversation(recipientId))
    }

    fun loadNotifications(startRealtime: Boolean = true) = load {
        runCatching { pushCoordinator.syncCurrentToken() }
        val page = repository.notifications()
        _state.value = _state.value.copy(notifications = page.data, unreadNotifications = repository.unreadCount())
        if (startRealtime) ensureNotificationRealtime()
    }

    fun markNotificationRead(id: String) = mutate("Notification marked read.", keepNotice = false) {
        repository.markNotificationRead(id)
        refreshNotifications()
    }

    fun markAllNotificationsRead() = mutate("All notifications marked read.") {
        repository.markAllNotificationsRead()
        refreshNotifications()
    }

    fun deleteNotification(id: String) = mutate("Notification deleted.", keepNotice = false) {
        repository.deleteNotification(id)
        refreshNotifications()
    }

    private fun ensureNotificationRealtime() {
        if (notificationJob != null) return
        repository.startNotificationRealtime()
        notificationJob = viewModelScope.launch {
            launch {
                repository.notificationRealtimeState.collect { connection ->
                    if (connection is RealtimeConnectionState.Authenticated) repository.ensureNotificationRealtimeListeners()
                }
            }
            repository.notificationChanges.collect { runCatching { refreshNotifications() } }
        }
    }

    private suspend fun refreshCommunity(id: String) {
        val community = repository.community(id)
        val membership = runCatching { repository.membership(community.id) }.getOrNull()
        val mayReadPrivate = community.visibility.uppercase() != "PRIVATE" || membership?.member == true
        _state.value = _state.value.copy(
            community = community,
            membership = membership,
            members = if (mayReadPrivate) runCatching { repository.members(community.id) }.getOrDefault(emptyList()) else emptyList(),
            communityEvents = if (mayReadPrivate) runCatching { repository.communityEvents(community.id) }.getOrDefault(emptyList()) else emptyList(),
        )
    }

    private suspend fun refreshNotifications() {
        val page = repository.notifications()
        _state.value = _state.value.copy(notifications = page.data, unreadNotifications = repository.unreadCount())
    }

    private fun load(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, notice = null)
            try { block() } catch (e: Exception) { _state.value = _state.value.copy(error = readable(e)) }
            finally { _state.value = _state.value.copy(loading = false) }
        }
    }

    private fun mutate(success: String, keepNotice: Boolean = true, block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(mutationBusy = true, error = null, notice = null)
            try {
                block()
                if (keepNotice) _state.value = _state.value.copy(notice = success)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = readable(e))
            } finally {
                _state.value = _state.value.copy(mutationBusy = false)
            }
        }
    }

    private fun readable(error: Exception): String = error.toVoiceCloudUserMessage("VoiceCloud Couldn’t Complete This Request. Try Again.")

    override fun onCleared() {
        repository.stopNotificationRealtime()
        super.onCleared()
    }
}
