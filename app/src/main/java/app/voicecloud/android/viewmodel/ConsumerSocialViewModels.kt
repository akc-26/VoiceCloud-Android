package app.voicecloud.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.core.data.BlocksRepository
import app.voicecloud.core.data.DiscoveryRepository
import app.voicecloud.core.data.NotificationsRepository
import app.voicecloud.core.data.ProfileRepository
import app.voicecloud.core.data.PublicProfileData
import app.voicecloud.core.data.PublicProfileRepository
import app.voicecloud.core.data.RankingsRepository
import app.voicecloud.core.data.ReferralsRepository
import app.voicecloud.core.data.SavedRoomsRepository
import app.voicecloud.core.data.TasksAchievementsRepository
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.model.ApiResult
import app.voicecloud.core.model.VoiceCloudUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PeopleUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val suggested: List<VCPersonUiModel> = emptyList(),
    val online: List<VCPersonUiModel> = emptyList(),
    val trending: List<VCPersonUiModel> = emptyList(),
)

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val discovery: DiscoveryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(PeopleUiState())
    val state: StateFlow<PeopleUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val suggested = discovery.suggestedPeople()
            val online = discovery.onlinePeople()
            val trending = discovery.trendingPeople()
            val error = listOf(suggested, online, trending).filterIsInstance<ApiResult.Failure>().firstOrNull()?.error?.message
            _state.value = PeopleUiState(
                isLoading = false,
                errorMessage = error,
                suggested = (suggested as? ApiResult.Success)?.data.orEmpty(),
                online = (online as? ApiResult.Success)?.data.orEmpty(),
                trending = (trending as? ApiResult.Success)?.data.orEmpty(),
            )
        }
    }
}

data class NotificationItemUi(
    val id: String,
    val title: String,
    val body: String?,
    val isRead: Boolean,
    val meta: String?,
)

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val unreadCount: Int = 0,
    val items: List<NotificationItemUi> = emptyList(),
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val list = repository.load()
            val unread = repository.unreadCount()
            val error = listOf(list, unread).filterIsInstance<ApiResult.Failure>().firstOrNull()?.error?.message
            _state.value = NotificationsUiState(
                isLoading = false,
                errorMessage = error,
                unreadCount = (unread as? ApiResult.Success)?.data ?: 0,
                items = (list as? ApiResult.Success)?.data.orEmpty().map {
                    NotificationItemUi(
                        id = it.id,
                        title = it.title,
                        body = it.body,
                        isRead = it.isRead,
                        meta = it.createdAt,
                    )
                },
            )
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            if (repository.markRead(id) is ApiResult.Success) {
                refresh()
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            if (repository.markAllRead() is ApiResult.Success) {
                refresh()
            }
        }
    }
}

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val displayName: String = "",
    val bio: String = "",
    val handle: String? = null,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = profileRepository.loadMyProfile()) {
                is ApiResult.Success -> {
                    val user = result.data
                    _state.value = EditProfileUiState(
                        isLoading = false,
                        displayName = user.displayName.orEmpty(),
                        bio = user.bio.orEmpty(),
                        handle = user.username?.let { "@$it" },
                    )
                }
                is ApiResult.Failure -> _state.value = EditProfileUiState(isLoading = false, errorMessage = result.error.message)
            }
        }
    }

    fun onDisplayNameChange(value: String) {
        _state.value = _state.value.copy(displayName = value, errorMessage = null)
    }

    fun onBioChange(value: String) {
        _state.value = _state.value.copy(bio = value, errorMessage = null)
    }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        viewModelScope.launch {
            _state.value = current.copy(isSaving = true, errorMessage = null)
            when (
                val result = profileRepository.updateProfile(
                    displayName = current.displayName.trim().ifBlank { null },
                    bio = current.bio.trim().ifBlank { null },
                )
            ) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isSaving = false)
                    onSaved()
                }
                is ApiResult.Failure -> _state.value = _state.value.copy(isSaving = false, errorMessage = result.error.message)
            }
        }
    }
}

data class SocialListUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val people: List<VCPersonUiModel> = emptyList(),
)

@HiltViewModel
class SocialListViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SocialListUiState())
    val state: StateFlow<SocialListUiState> = _state.asStateFlow()

    fun loadFollowers(userId: String) {
        load(userId) { profileRepository.followers(it) }
    }

    fun loadFollowing(userId: String) {
        load(userId) { profileRepository.following(it) }
    }

    private fun load(userId: String, call: suspend (String) -> ApiResult<List<VCPersonUiModel>>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = call(userId)) {
                is ApiResult.Success -> _state.value = SocialListUiState(isLoading = false, people = result.data)
                is ApiResult.Failure -> _state.value = SocialListUiState(isLoading = false, errorMessage = result.error.message)
            }
        }
    }
}

data class PublicProfileUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val profile: PublicProfileData? = null,
    val isSelf: Boolean = false,
    val socialActionInFlight: Boolean = false,
)

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val publicProfileRepository: PublicProfileRepository,
    private val profileRepository: ProfileRepository,
    private val blocksRepository: BlocksRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(PublicProfileUiState())
    val state: StateFlow<PublicProfileUiState> = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = PublicProfileUiState(isLoading = true)
            val myId = when (val me = profileRepository.loadMyProfile()) {
                is ApiResult.Success -> me.data.id
                is ApiResult.Failure -> null
            }
            when (val result = publicProfileRepository.load(userId)) {
                is ApiResult.Success -> {
                    _state.value = PublicProfileUiState(
                        isLoading = false,
                        profile = result.data,
                        isSelf = myId != null && myId == result.data.user.id,
                    )
                }
                is ApiResult.Failure -> _state.value = PublicProfileUiState(isLoading = false, errorMessage = result.error.message)
            }
        }
    }

    fun toggleFollow(userId: String) {
        val current = _state.value.profile ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(socialActionInFlight = true, errorMessage = null)
            val result = if (current.isFollowing) profileRepository.unfollow(userId) else profileRepository.follow(userId)
            when (result) {
                is ApiResult.Success -> load(userId)
                is ApiResult.Failure -> _state.value = _state.value.copy(
                    socialActionInFlight = false,
                    errorMessage = result.error.message,
                )
            }
        }
    }

    fun block(userId: String, onBlocked: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(socialActionInFlight = true, errorMessage = null)
            when (val result = blocksRepository.block(userId)) {
                is ApiResult.Success -> onBlocked()
                is ApiResult.Failure -> _state.value = _state.value.copy(
                    socialActionInFlight = false,
                    errorMessage = result.error.message,
                )
            }
        }
    }
}

data class SavedRoomsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val rooms: List<VCRoomUiModel> = emptyList(),
)

@HiltViewModel
class SavedRoomsViewModel @Inject constructor(
    private val repository: SavedRoomsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SavedRoomsUiState())
    val state: StateFlow<SavedRoomsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.load()) {
                is ApiResult.Success -> _state.value = SavedRoomsUiState(isLoading = false, rooms = result.data)
                is ApiResult.Failure -> _state.value = SavedRoomsUiState(isLoading = false, errorMessage = result.error.message)
            }
        }
    }
}

data class RankingsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val leaderboard: List<VCPersonUiModel> = emptyList(),
    val trending: List<VCPersonUiModel> = emptyList(),
)

@HiltViewModel
class RankingsViewModel @Inject constructor(
    private val repository: RankingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(RankingsUiState())
    val state: StateFlow<RankingsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val leaderboard = repository.leaderboardUsers()
            val trending = repository.trendingUsers()
            val error = listOf(leaderboard, trending).filterIsInstance<ApiResult.Failure>().firstOrNull()?.error?.message
            _state.value = RankingsUiState(
                isLoading = false,
                errorMessage = error,
                leaderboard = (leaderboard as? ApiResult.Success)?.data.orEmpty(),
                trending = (trending as? ApiResult.Success)?.data.orEmpty(),
            )
        }
    }
}

data class BlockedUsersUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val people: List<VCPersonUiModel> = emptyList(),
    val actionInFlight: Boolean = false,
)

@HiltViewModel
class BlockedUsersViewModel @Inject constructor(
    private val repository: BlocksRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BlockedUsersUiState())
    val state: StateFlow<BlockedUsersUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.blockedUsers()) {
                is ApiResult.Success -> _state.value = BlockedUsersUiState(isLoading = false, people = result.data)
                is ApiResult.Failure -> _state.value = BlockedUsersUiState(isLoading = false, errorMessage = result.error.message)
            }
        }
    }

    fun unblock(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInFlight = true)
            when (repository.unblock(userId)) {
                is ApiResult.Success -> refresh()
                is ApiResult.Failure -> _state.value = _state.value.copy(actionInFlight = false)
            }
        }
    }
}

data class ReferralsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val code: String? = null,
    val inviteCount: Int? = null,
    val rewardSummary: String? = null,
)

@HiltViewModel
class ReferralsViewModel @Inject constructor(
    private val repository: ReferralsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ReferralsUiState())
    val state: StateFlow<ReferralsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.summary()) {
                is ApiResult.Success -> {
                    val body = result.data
                    _state.value = ReferralsUiState(
                        isLoading = false,
                        code = body.code,
                        inviteCount = body.totalReferrals,
                        rewardSummary = body.rewardsEarned?.let { "$it rewards earned" },
                    )
                }
                is ApiResult.Failure -> _state.value = ReferralsUiState(isLoading = false, errorMessage = result.error.message)
            }
        }
    }
}

data class TasksHubUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val tasks: List<app.voicecloud.core.model.TaskItemDto> = emptyList(),
    val achievements: List<app.voicecloud.core.model.AchievementItemDto> = emptyList(),
)

@HiltViewModel
class TasksHubViewModel @Inject constructor(
    private val repository: TasksAchievementsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TasksHubUiState())
    val state: StateFlow<TasksHubUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val tasks = repository.tasks()
            val achievements = repository.achievements()
            val error = listOf(tasks, achievements).filterIsInstance<ApiResult.Failure>().firstOrNull()?.error?.message
            _state.value = TasksHubUiState(
                isLoading = false,
                errorMessage = error,
                tasks = (tasks as? ApiResult.Success)?.data.orEmpty(),
                achievements = (achievements as? ApiResult.Success)?.data.orEmpty(),
            )
        }
    }
}
