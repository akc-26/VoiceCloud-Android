package app.voicecloud.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.core.data.ActivityHistoryRepository
import app.voicecloud.core.data.ClubsRepository
import app.voicecloud.core.data.EventsRepository
import app.voicecloud.core.data.FriendRequestItem
import app.voicecloud.core.data.FriendsRepository
import app.voicecloud.core.data.UserSettingsRepository
import app.voicecloud.core.data.VisitorsRepository
import app.voicecloud.core.data.ActivityHistoryItem
import app.voicecloud.core.designsystem.component.VCCommunityUiModel
import app.voicecloud.core.designsystem.component.VCEventUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.model.ApiResult
import app.voicecloud.core.data.mapper.toEventUiModel
import app.voicecloud.core.model.ScheduledRoomDto
import app.voicecloud.core.model.UpdateUserSettingsRequest
import app.voicecloud.core.model.UserSettingsDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val friends: List<VCPersonUiModel> = emptyList(),
    val pending: List<FriendRequestItem> = emptyList(),
    val suggested: List<VCPersonUiModel> = emptyList(),
    val actionInFlight: Boolean = false,
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: FriendsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FriendsUiState())
    val state: StateFlow<FriendsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val friends = repository.friends()
            val pending = repository.pendingRequests()
            val suggested = repository.suggested()
            val error = listOf(friends, pending, suggested).filterIsInstance<ApiResult.Failure>().firstOrNull()?.error?.message
            _state.value = FriendsUiState(
                isLoading = false,
                errorMessage = error,
                friends = (friends as? ApiResult.Success)?.data.orEmpty(),
                pending = (pending as? ApiResult.Success)?.data.orEmpty(),
                suggested = (suggested as? ApiResult.Success)?.data.orEmpty(),
            )
        }
    }

    fun accept(requestId: String) = act { repository.accept(requestId) }
    fun reject(requestId: String) = act { repository.reject(requestId) }
    fun sendRequest(userId: String) = act { repository.sendRequest(userId) }
    fun removeFriend(userId: String) = act { repository.removeFriend(userId) }

    private fun act(block: suspend () -> ApiResult<Unit>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInFlight = true)
            when (block()) {
                is ApiResult.Success -> refresh()
                is ApiResult.Failure -> _state.value = _state.value.copy(actionInFlight = false)
            }
        }
    }
}

data class ProfileVisitorsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val totalVisitors: Int = 0,
    val visitors: List<VCPersonUiModel> = emptyList(),
)

@HiltViewModel
class ProfileVisitorsViewModel @Inject constructor(
    private val repository: VisitorsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileVisitorsUiState())
    val state: StateFlow<ProfileVisitorsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val list = repository.visitors()
            val stats = repository.statsTotal()
            val error = listOf(list, stats).filterIsInstance<ApiResult.Failure>().firstOrNull()?.error?.message
            _state.value = ProfileVisitorsUiState(
                isLoading = false,
                errorMessage = error,
                totalVisitors = (stats as? ApiResult.Success)?.data ?: 0,
                visitors = (list as? ApiResult.Success)?.data.orEmpty(),
            )
        }
    }
}

data class ActivityHistoryUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val items: List<ActivityHistoryItem> = emptyList(),
)

@HiltViewModel
class ActivityHistoryViewModel @Inject constructor(
    private val repository: ActivityHistoryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ActivityHistoryUiState())
    val state: StateFlow<ActivityHistoryUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.load()) {
                is ApiResult.Success -> _state.value = ActivityHistoryUiState(isLoading = false, items = result.data)
                is ApiResult.Failure -> _state.value = ActivityHistoryUiState(isLoading = false, errorMessage = result.error.message)
            }
        }
    }
}

data class NotificationPreferencesUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val socialNotifications: Boolean = true,
    val messageNotifications: Boolean = true,
    val roomNotifications: Boolean = true,
    val marketingNotifications: Boolean = false,
)

@HiltViewModel
class NotificationPreferencesViewModel @Inject constructor(
    private val repository: UserSettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationPreferencesUiState())
    val state: StateFlow<NotificationPreferencesUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.load()) {
                is ApiResult.Success -> _state.value = result.data.toUi()
                is ApiResult.Failure -> _state.value = NotificationPreferencesUiState(isLoading = false, errorMessage = result.error.message)
            }
        }
    }

    fun togglePush(value: Boolean) = update { copy(pushEnabled = value) }
    fun toggleEmail(value: Boolean) = update { copy(emailEnabled = value) }
    fun toggleSocial(value: Boolean) = update { copy(socialNotifications = value) }
    fun toggleMessages(value: Boolean) = update { copy(messageNotifications = value) }
    fun toggleRooms(value: Boolean) = update { copy(roomNotifications = value) }
    fun toggleMarketing(value: Boolean) = update { copy(marketingNotifications = value) }

    private fun update(mutate: NotificationPreferencesUiState.() -> NotificationPreferencesUiState) {
        val next = _state.value.mutate()
        _state.value = next
        viewModelScope.launch {
            _state.value = next.copy(isSaving = true, errorMessage = null)
            when (repository.update(next.toRequest())) {
                is ApiResult.Success -> _state.value = _state.value.copy(isSaving = false)
                is ApiResult.Failure -> _state.value = _state.value.copy(isSaving = false, errorMessage = "Could not save preferences.")
            }
        }
    }

    private fun UserSettingsDto.toUi() = NotificationPreferencesUiState(
        isLoading = false,
        pushEnabled = pushEnabled ?: true,
        emailEnabled = emailEnabled ?: true,
        socialNotifications = socialNotifications ?: true,
        messageNotifications = messageNotifications ?: true,
        roomNotifications = roomNotifications ?: true,
        marketingNotifications = marketingNotifications ?: false,
    )

    private fun NotificationPreferencesUiState.toRequest() = UpdateUserSettingsRequest(
        pushEnabled = pushEnabled,
        emailEnabled = emailEnabled,
        socialNotifications = socialNotifications,
        messageNotifications = messageNotifications,
        roomNotifications = roomNotifications,
        marketingNotifications = marketingNotifications,
    )
}

data class CommunitiesUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val communities: List<VCCommunityUiModel> = emptyList(),
)

@HiltViewModel
class CommunitiesViewModel @Inject constructor(
    private val repository: ClubsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CommunitiesUiState())
    val state: StateFlow<CommunitiesUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.list()) {
                is ApiResult.Success -> _state.value = CommunitiesUiState(isLoading = false, communities = result.data)
                is ApiResult.Failure -> _state.value = CommunitiesUiState(isLoading = false, errorMessage = result.error.message)
            }
        }
    }
}

data class CommunityDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val community: VCCommunityUiModel? = null,
    val members: List<VCPersonUiModel> = emptyList(),
    val rooms: List<VCEventUiModel> = emptyList(),
    val membershipActionInFlight: Boolean = false,
)

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    private val repository: ClubsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CommunityDetailUiState())
    val state: StateFlow<CommunityDetailUiState> = _state.asStateFlow()

    fun load(clubId: String) {
        viewModelScope.launch {
            _state.value = CommunityDetailUiState(isLoading = true)
            val detail = repository.detail(clubId)
            val members = repository.members(clubId)
            val rooms = repository.rooms(clubId)
            val error = listOf(detail, members, rooms).filterIsInstance<ApiResult.Failure>().firstOrNull()?.error?.message
            _state.value = CommunityDetailUiState(
                isLoading = false,
                errorMessage = error,
                community = (detail as? ApiResult.Success)?.data,
                members = (members as? ApiResult.Success)?.data.orEmpty(),
                rooms = (rooms as? ApiResult.Success)?.data.orEmpty(),
            )
        }
    }

    fun join(clubId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(membershipActionInFlight = true)
            when (repository.join(clubId)) {
                is ApiResult.Success -> load(clubId)
                is ApiResult.Failure -> _state.value = _state.value.copy(membershipActionInFlight = false)
            }
        }
    }

    fun leave(clubId: String, onLeft: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(membershipActionInFlight = true)
            when (repository.leave(clubId)) {
                is ApiResult.Success -> onLeft()
                is ApiResult.Failure -> _state.value = _state.value.copy(membershipActionInFlight = false)
            }
        }
    }
}

data class CreateCommunityUiState(
    val name: String = "",
    val description: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class CreateCommunityViewModel @Inject constructor(
    private val repository: ClubsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CreateCommunityUiState())
    val state: StateFlow<CreateCommunityUiState> = _state.asStateFlow()

    fun onNameChange(value: String) { _state.value = _state.value.copy(name = value, errorMessage = null) }
    fun onDescriptionChange(value: String) { _state.value = _state.value.copy(description = value, errorMessage = null) }

    fun submit(onCreated: (String) -> Unit) {
        val name = _state.value.name.trim()
        if (name.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Community name is required.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
            when (val result = repository.create(name, _state.value.description.trim().ifBlank { null })) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isSubmitting = false)
                    result.data.id?.let { onCreated(it) }
                }
                is ApiResult.Failure -> _state.value = _state.value.copy(isSubmitting = false, errorMessage = result.error.message)
            }
        }
    }
}

data class EventsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val events: List<VCEventUiModel> = emptyList(),
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: EventsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EventsUiState())
    val state: StateFlow<EventsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.list()) {
                is ApiResult.Success -> _state.value = EventsUiState(isLoading = false, events = result.data)
                is ApiResult.Failure -> _state.value = EventsUiState(isLoading = false, errorMessage = result.error.message)
            }
        }
    }
}

data class EventDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val event: VCEventUiModel? = null,
    val description: String? = null,
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val repository: EventsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EventDetailUiState())
    val state: StateFlow<EventDetailUiState> = _state.asStateFlow()

    fun load(eventId: String) {
        viewModelScope.launch {
            _state.value = EventDetailUiState(isLoading = true)
            when (val result = repository.detailRaw(eventId)) {
                is ApiResult.Success -> {
                    val dto = result.data
                    _state.value = EventDetailUiState(
                        isLoading = false,
                        event = dto.toEventUiModel(),
                        description = dto.description,
                    )
                }
                is ApiResult.Failure -> _state.value = EventDetailUiState(isLoading = false, errorMessage = result.error.message)
            }
        }
    }
}
