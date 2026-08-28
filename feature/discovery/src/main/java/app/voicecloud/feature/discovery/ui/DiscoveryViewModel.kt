package app.voicecloud.feature.discovery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.feature.discovery.data.DiscoveryRepository
import app.voicecloud.feature.discovery.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoveryUiState(
    val loading: Boolean = false,
    val mutationBusy: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
    val viewer: ViewerIdentity = ViewerIdentity(),
    val home: HomeSnapshot = HomeSnapshot(),
    val explore: ExploreSnapshot = ExploreSnapshot(),
    val rooms: List<VoiceCloudRoom> = emptyList(),
    val people: List<VoiceCloudUser> = emptyList(),
    val searchLanding: SearchLandingSnapshot = SearchLandingSnapshot(),
    val search: SearchSnapshot = SearchSnapshot(),
    val profile: VoiceCloudProfile? = null,
    val myProfile: VoiceCloudProfile? = null,
    val socialUsers: List<VoiceCloudUser> = emptyList(),
    val friends: List<FriendListItem> = emptyList(),
    val pending: PendingFriendRequests = PendingFriendRequests(),
    val friendSuggestions: List<VoiceCloudUser> = emptyList(),
)

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val repository: DiscoveryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DiscoveryUiState())
    val state: StateFlow<DiscoveryUiState> = _state.asStateFlow()

    fun setViewer(id: String?, username: String?) {
        val next = ViewerIdentity(id = id, username = username)
        if (_state.value.viewer != next) _state.value = _state.value.copy(viewer = next)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(error = null, notice = null)
    }

    fun loadHome() = load { viewer ->
        _state.value = _state.value.copy(home = repository.home(viewer))
    }

    fun loadExplore() = load { viewer ->
        _state.value = _state.value.copy(explore = repository.explore(viewer))
    }

    fun loadRooms(category: String? = null) = load {
        _state.value = _state.value.copy(rooms = repository.liveRooms(category))
    }

    fun loadPeople(creatorsOnly: Boolean) = load { viewer ->
        _state.value = _state.value.copy(people = repository.people(viewer, creatorsOnly))
    }

    fun loadSearchLanding() = load { viewer ->
        _state.value = _state.value.copy(searchLanding = repository.searchLanding(viewer))
    }

    fun search(query: String) = load { viewer ->
        _state.value = _state.value.copy(search = repository.search(query, viewer))
    }

    fun loadPublicProfile(username: String) = load { viewer ->
        _state.value = _state.value.copy(profile = repository.publicProfile(username, viewer))
    }

    fun loadMyProfile() = load {
        _state.value = _state.value.copy(myProfile = repository.myProfile())
    }

    fun followProfile() {
        val profile = _state.value.profile ?: return
        val shouldFollow = !(profile.relationship?.isFollowing ?: false)
        mutate("Relationship updated.") {
            repository.setFollowing(profile.id, shouldFollow)
            val refreshed = repository.publicProfile(profile.username, _state.value.viewer)
            _state.value = _state.value.copy(profile = refreshed)
        }
    }

    fun loadSocial(mode: String, search: String = "") = load { viewer ->
        val users = if (mode == "followers") repository.followers(viewer, search) else repository.following(viewer, search)
        _state.value = _state.value.copy(socialUsers = users)
    }

    fun unfollowFromList(userId: String, search: String = "") = mutate("Unfollowed.") {
        repository.setFollowing(userId, false)
        _state.value = _state.value.copy(socialUsers = repository.following(_state.value.viewer, search))
    }

    fun loadFriends() = load { viewer ->
        val friends = repository.friends(viewer)
        val pending = repository.pending(viewer)
        val suggestions = repository.suggestedFriends(viewer)
        _state.value = _state.value.copy(friends = friends, pending = pending, friendSuggestions = suggestions)
    }

    fun sendFriendRequest(userId: String) = mutate("Friend request sent.") {
        repository.sendFriendRequest(userId)
        refreshFriendsAfterMutation()
    }

    fun acceptFriendRequest(requestId: String) = mutate("Friend request accepted.") {
        repository.acceptFriendRequest(requestId)
        refreshFriendsAfterMutation()
    }

    fun rejectFriendRequest(requestId: String) = mutate("Friend request declined.") {
        repository.rejectFriendRequest(requestId)
        refreshFriendsAfterMutation()
    }

    fun cancelFriendRequest(requestId: String) = mutate("Friend request cancelled.") {
        repository.cancelFriendRequest(requestId)
        refreshFriendsAfterMutation()
    }

    fun removeFriend(userId: String) = mutate("Friend removed.") {
        repository.removeFriend(userId)
        refreshFriendsAfterMutation()
    }

    private suspend fun refreshFriendsAfterMutation() {
        val viewer = _state.value.viewer
        _state.value = _state.value.copy(
            friends = repository.friends(viewer),
            pending = repository.pending(viewer),
            friendSuggestions = repository.suggestedFriends(viewer),
        )
    }

    private fun load(block: suspend (ViewerIdentity) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, notice = null)
            try {
                block(_state.value.viewer)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = readableError(e))
            } finally {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    private fun mutate(successMessage: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(mutationBusy = true, error = null, notice = null)
            try {
                block()
                _state.value = _state.value.copy(notice = successMessage)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = readableError(e))
            } finally {
                _state.value = _state.value.copy(mutationBusy = false)
            }
        }
    }

    private fun readableError(error: Exception): String = when {
        error.message.isNullOrBlank() -> "VoiceCloud could not load this content."
        else -> error.message!!
    }
}
