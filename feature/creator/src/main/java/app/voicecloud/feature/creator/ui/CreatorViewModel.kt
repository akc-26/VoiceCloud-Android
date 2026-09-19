package app.voicecloud.feature.creator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.feature.creator.data.CreatorRepository
import app.voicecloud.feature.creator.model.*
import app.voicecloud.feature.discovery.data.DiscoveryRepository
import app.voicecloud.feature.discovery.model.ViewerIdentity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class CreatorViewModel @Inject constructor(
    private val repository: CreatorRepository,
    private val discoveryRepository: DiscoveryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CreatorUiState())
    val state: StateFlow<CreatorUiState> = _state.asStateFlow()
    private val _events = Channel<CreatorEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var activeLoad: Job? = null
    private var loadGeneration: Long = 0

    fun loadDashboard() = load {
        val maintenance = repository.maintenance()
        if (maintenance.enabled) {
            _events.send(CreatorEvent.AuthFailure(503, maintenance.message))
            return@load
        }
        _state.value = _state.value.copy(dashboard = repository.dashboard())
    }

    fun loadProfile() = load { _state.value = _state.value.copy(profile = repository.profile()) }

    fun saveProfile(bio: String?, country: String?, interests: List<String>) = save("Creator Profile Updated.", "We Couldn’t Update Your Creator Profile. Try Again.") {
        _state.value = _state.value.copy(profile = repository.updateProfile(bio, country, interests))
    }

    fun loadSettings() = load { _state.value = _state.value.copy(settings = repository.settings()) }

    fun saveSettings(value: CreatorSettings) = save("Creator Settings Updated.", "We Couldn’t Save Your Creator Settings. Try Again.") {
        _state.value = _state.value.copy(settings = repository.updateSettings(value))
    }

    fun loadCmsPages() = load { _state.value = _state.value.copy(cmsPages = repository.cmsPages()) }

    fun loadCmsPage(slug: String) = load { _state.value = _state.value.copy(cmsPage = repository.cmsPage(slug)) }

    fun contact(name: String, email: String, phone: String?, description: String) = save("Your Message Was Sent To VoiceCloud Support.", "We Couldn’t Send Your Message. Check The Details And Try Again.") {
        repository.contact(name, email, phone, description)
    }

    fun loadAudience(viewerId: String?, viewerUsername: String?) = load {
        val viewer = ViewerIdentity(viewerId, viewerUsername)
        val followers = discoveryRepository.allFollowers(viewer)
        val following = discoveryRepository.allFollowing(viewer)
        val subscriberPage = repository.subscribers(page = 1, limit = 1)
        val dashboard = runCatching { repository.dashboard() }.getOrNull()
        val listeners = dashboard?.metrics?.firstOrNull { it.key == "listeners" }?.value
        _state.value = _state.value.copy(
            audience = CreatorAudience(
                followerTotal = followers.total.coerceAtLeast(followers.data.size),
                followingTotal = following.total.coerceAtLeast(following.data.size),
                subscriberTotal = subscriberPage.total,
                listeners = listeners,
            ),
        )
    }

    fun loadFollowers(viewerId: String?, viewerUsername: String?, search: String = "", sort: CreatorFollowerSort = CreatorFollowerSort.NAME) = load {
        val viewer = ViewerIdentity(viewerId, viewerUsername)
        val followerPage = discoveryRepository.allFollowers(viewer, search)
        val followingIds = discoveryRepository.allFollowing(viewer).data.map { it.id }.toSet()
        val mapped = followerPage.data.map { CreatorFollower(it, it.id in followingIds) }
        val sorted = when (sort) {
            CreatorFollowerSort.NAME -> mapped.sortedBy { it.user.displayName.ifBlank { it.user.username }.lowercase() }
            CreatorFollowerSort.POPULARITY -> mapped.sortedByDescending { it.user.popularityScore }
            CreatorFollowerSort.ONLINE -> mapped.sortedWith(compareByDescending<CreatorFollower> { it.user.isOnline }.thenBy { it.user.displayName.ifBlank { it.user.username }.lowercase() })
        }
        _state.value = _state.value.copy(followers = sorted, followerSearch = search.trim(), followerSort = sort)
    }

    fun setFollowBack(userId: String, follow: Boolean, viewerId: String?, viewerUsername: String?) = save(
        if (follow) "Follower Followed Back." else "Follower Unfollowed.",
        "We Couldn’t Update This Follow Relationship. Try Again.",
    ) {
        val result = discoveryRepository.setFollowing(userId, follow)
        val current = _state.value
        val updated = current.followers.map { item ->
            if (item.user.id == userId) item.copy(followingBack = result.isFollowing) else item
        }
        _state.value = current.copy(followers = updated)
        // Reconcile counts from the backend after the mutation without inventing local totals.
        val viewer = ViewerIdentity(viewerId, viewerUsername)
        val following = discoveryRepository.allFollowing(viewer)
        _state.value = _state.value.copy(audience = _state.value.audience?.copy(followingTotal = following.total.coerceAtLeast(following.data.size)))
    }

    fun loadPlans() = load { _state.value = _state.value.copy(plans = repository.plans()) }

    fun createPlan(title: String, description: String?, monthlyPrice: Double, yearlyPrice: Double?, benefits: List<String>, visibility: String) = save(
        "Subscription Plan Created.", "We Couldn’t Create This Subscription Plan. Check The Details And Try Again."
    ) { _state.value = _state.value.copy(plans = repository.createPlan(title, description, monthlyPrice, yearlyPrice, benefits, visibility)) }

    fun updatePlan(id: String, title: String, description: String?, monthlyPrice: Double, yearlyPrice: Double?, benefits: List<String>, visibility: String, status: String) = save(
        "Subscription Plan Updated.", "We Couldn’t Update This Subscription Plan. Check The Details And Try Again."
    ) { _state.value = _state.value.copy(plans = repository.updatePlan(id, title, description, monthlyPrice, yearlyPrice, benefits, visibility, status)) }

    fun archivePlan(id: String) = save("Subscription Plan Archived.", "We Couldn’t Archive This Subscription Plan. Try Again.") {
        _state.value = _state.value.copy(plans = repository.archivePlan(id))
    }

    fun loadSubscribers(status: String? = null) = load {
        _state.value = _state.value.copy(subscribers = repository.subscribers(status = status), subscriberStatus = status?.uppercase())
    }

    fun loadAnalytics() = load { _state.value = _state.value.copy(analytics = repository.analytics()) }

    fun loadWallet() = load { _state.value = _state.value.copy(wallet = repository.wallet()) }

    fun loadEarnings() = load { _state.value = _state.value.copy(earnings = repository.earnings()) }

    fun loadGifts() = load { _state.value = _state.value.copy(gifts = repository.gifts()) }

    fun loadPayouts(status: String? = null) = load {
        _state.value = _state.value.copy(payouts = repository.payoutRequests(status = status))
    }

    fun loadPayout(id: String) = load {
        _state.value = _state.value.copy(selectedPayout = repository.payoutRequest(id))
    }

    fun createPayout(diamondAmount: Int, payoutMethod: String) = save(
        "Payout Request Submitted.", "We Couldn’t Submit This Payout Request. Check The Details And Try Again."
    ) {
        _state.value = _state.value.copy(payouts = repository.createPayoutRequest(diamondAmount, payoutMethod))
    }

    fun clearCmsPage() { _state.value = _state.value.copy(cmsPage = null) }
    fun clearFeedback() { _state.value = _state.value.copy(error = null, notice = null) }

    private fun load(block: suspend () -> Unit) {
        val generation = ++loadGeneration
        activeLoad?.cancel()
        activeLoad = viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, notice = null)
            try { block() } catch (error: Throwable) { handle(error) }
            finally {
                if (generation == loadGeneration) _state.value = _state.value.copy(loading = false)
            }
        }
    }

    private fun save(notice: String, fallback: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, error = null, notice = null)
            try {
                block()
                _state.value = _state.value.copy(notice = notice)
            } catch (error: Throwable) { handle(error, fallback) }
            finally { _state.value = _state.value.copy(saving = false) }
        }
    }

    private suspend fun handle(error: Throwable, fallback: String = "We Couldn’t Complete This Creator Request. Try Again.") {
        if (error is HttpException && error.code() in setOf(401, 403, 503)) {
            _events.send(CreatorEvent.AuthFailure(error.code(), safeMessage(error, fallback)))
            return
        }
        _state.value = _state.value.copy(error = safeMessage(error, fallback))
    }

    private fun safeMessage(error: Throwable, fallback: String): String {
        val message = error.message.orEmpty().trim()
        val unsafe = listOf("sql", "postgres", "typeorm", "constraint", "stack trace", "exception at", "relation ", "column ", "http 4", "http 5")
        return when {
            error is HttpException && error.code() == 401 -> "Your Session Has Expired. Sign In Again."
            error is HttpException && error.code() == 403 -> "This Creator Action Isn’t Available For Your Account."
            error is HttpException && error.code() == 429 -> "Too Many Requests. Try Again In A Moment."
            error is HttpException && error.code() >= 500 -> "VoiceCloud Is Temporarily Unavailable. Try Again Soon."
            error is HttpException -> fallback
            message.isBlank() || unsafe.any { message.contains(it, ignoreCase = true) } -> fallback
            else -> message
        }
    }
}
