package app.voicecloud.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.android.ui.consumer.ConsumerExploreUiState
import app.voicecloud.android.ui.consumer.ConsumerHomeUiState
import app.voicecloud.android.ui.consumer.ConsumerSearchUiState
import app.voicecloud.android.ui.economy.ConsumerEconomyUiState
import app.voicecloud.android.ui.messaging.ConsumerConversationUiState
import app.voicecloud.android.ui.messaging.ConsumerMessagesUiState
import app.voicecloud.android.ui.messaging.VCConversationUiModel
import app.voicecloud.android.ui.profile.ConsumerProfileUiState
import app.voicecloud.core.data.ChatRepository
import app.voicecloud.core.data.DiscoveryRepository
import app.voicecloud.core.data.ProfileRepository
import app.voicecloud.core.data.EventsRepository
import app.voicecloud.core.data.SearchRepository
import app.voicecloud.android.ui.economy.EconomyTransactionUiModel
import app.voicecloud.core.data.WalletRepository
import app.voicecloud.core.model.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val discovery: DiscoveryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ConsumerHomeUiState(isLoading = true))
    val state: StateFlow<ConsumerHomeUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val live = discovery.liveRooms()
            val trending = discovery.trendingRooms()
            val people = discovery.popularPeople()
            val hosts = discovery.trendingHosts()
            val error = listOf(live, trending, people, hosts).filterIsInstance<ApiResult.Failure>().firstOrNull()?.error?.message
            _state.value = ConsumerHomeUiState(
                isLoading = false,
                errorMessage = error,
                liveRooms = (live as? ApiResult.Success)?.data.orEmpty(),
                recommendedRooms = (trending as? ApiResult.Success)?.data.orEmpty(),
                people = ((people as? ApiResult.Success)?.data.orEmpty() + (hosts as? ApiResult.Success)?.data.orEmpty()).distinctBy { it.userId ?: it.name },
            )
        }
    }
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val discovery: DiscoveryRepository,
    private val eventsRepository: EventsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ConsumerExploreUiState(isLoading = true))
    val state: StateFlow<ConsumerExploreUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val popular = discovery.popularRooms()
            val live = discovery.liveRooms()
            val people = discovery.popularPeople()
            val hosts = discovery.trendingHosts()
            val trendingPeople = discovery.trendingPeople()
            val events = eventsRepository.list()
            val error = listOf(popular, live, people, hosts, trendingPeople, events).filterIsInstance<ApiResult.Failure>().firstOrNull()?.error?.message
            _state.value = ConsumerExploreUiState(
                isLoading = false,
                errorMessage = error,
                liveRooms = (live as? ApiResult.Success)?.data.orEmpty(),
                trendingRooms = (popular as? ApiResult.Success)?.data.orEmpty(),
                people = ((people as? ApiResult.Success)?.data.orEmpty() + (hosts as? ApiResult.Success)?.data.orEmpty() + (trendingPeople as? ApiResult.Success)?.data.orEmpty())
                    .distinctBy { it.userId ?: it.name },
                events = (events as? ApiResult.Success)?.data.orEmpty(),
            )
        }
    }
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ConsumerSearchUiState())
    val state: StateFlow<ConsumerSearchUiState> = _state.asStateFlow()

    fun onQueryChange(query: String) {
        _state.value = _state.value.copy(query = query, errorMessage = null)
    }

    fun search() {
        val query = _state.value.query
        viewModelScope.launch {
            _state.value = _state.value.copy(isSearching = true, isLoading = true, errorMessage = null)
            when (val result = searchRepository.search(query)) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    isSearching = true,
                    rooms = result.data.rooms,
                    people = result.data.people,
                )
                is ApiResult.Failure -> _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = result.error.message,
                )
            }
        }
    }
}

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ConsumerMessagesUiState(isLoading = true))
    val state: StateFlow<ConsumerMessagesUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = chatRepository.conversations()) {
                is ApiResult.Success -> _state.value = ConsumerMessagesUiState(
                    isLoading = false,
                    conversations = result.data.map {
                        VCConversationUiModel(
                            id = it.id,
                            title = it.title,
                            preview = it.preview,
                            metaLabel = it.unreadCount.takeIf { count -> count > 0 }?.let { count -> "$count unread" },
                        )
                    },
                )
                is ApiResult.Failure -> _state.value = ConsumerMessagesUiState(
                    isLoading = false,
                    errorMessage = result.error.message,
                )
            }
        }
    }
}

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<ConsumerConversationUiState?>(null)
    val state: StateFlow<ConsumerConversationUiState?> = _state.asStateFlow()

    fun bind(conversationId: String, title: String) {
        if (_state.value?.conversationId == conversationId) return
        _state.value = ConsumerConversationUiState(conversationId = conversationId, title = title, isLoading = true)
        load(conversationId)
    }

    fun onComposerChange(text: String) {
        val current = _state.value ?: return
        _state.value = current.copy(composerText = text)
        if (text.isNotBlank()) {
            viewModelScope.launch { chatRepository.sendTyping(current.conversationId) }
        }
    }

    fun send() {
        val current = _state.value ?: return
        val text = current.composerText.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            _state.value = current.copy(isSending = true, errorMessage = null)
            when (val result = chatRepository.sendMessage(current.conversationId, text)) {
                is ApiResult.Success -> {
                    val updated = current.messages + app.voicecloud.android.ui.messaging.VCMessageUiModel(
                        id = result.data.id,
                        text = result.data.text,
                        outgoing = result.data.outgoing,
                        metaLabel = result.data.metaLabel,
                    )
                    _state.value = current.copy(messages = updated, composerText = "", isSending = false)
                    chatRepository.sendTyping(current.conversationId)
                }
                is ApiResult.Failure -> _state.value = current.copy(isSending = false, errorMessage = result.error.message)
            }
        }
    }

    fun retryLoad() {
        val id = _state.value?.conversationId ?: return
        load(id)
    }

    private fun load(conversationId: String) {
        viewModelScope.launch {
            when (val result = chatRepository.messages(conversationId)) {
                is ApiResult.Success -> {
                    chatRepository.markRead(conversationId)
                    _state.value = _state.value?.copy(
                        isLoading = false,
                        messages = result.data.map {
                            app.voicecloud.android.ui.messaging.VCMessageUiModel(
                                id = it.id,
                                text = it.text,
                                outgoing = it.outgoing,
                                metaLabel = it.metaLabel,
                            )
                        },
                    )
                }
                is ApiResult.Failure -> _state.value = _state.value?.copy(isLoading = false, errorMessage = result.error.message)
            }
        }
    }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ConsumerProfileUiState(isLoading = true))
    val state: StateFlow<ConsumerProfileUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = profileRepository.loadMyProfile()) {
                is ApiResult.Success -> {
                    val user = result.data
                    _state.value = ConsumerProfileUiState(
                        isLoading = false,
                        displayName = user.displayName ?: user.username,
                        handle = user.username?.let { "@$it" },
                        bio = user.bio,
                        notificationsAvailable = true,
                    )
                }
                is ApiResult.Failure -> _state.value = ConsumerProfileUiState(
                    isLoading = false,
                    errorMessage = result.error.message,
                )
            }
        }
    }
}

@HiltViewModel
class EconomyViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ConsumerEconomyUiState(isLoading = true))
    val state: StateFlow<ConsumerEconomyUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = walletRepository.overview()) {
                is ApiResult.Success -> _state.value = ConsumerEconomyUiState(
                    isLoading = false,
                    balanceLabel = result.data.balanceLabel,
                    transactions = result.data.transactions.mapIndexed { index, line ->
                        EconomyTransactionUiModel(
                            id = "tx-$index",
                            title = line,
                            amountLabel = "",
                        )
                    },
                )
                is ApiResult.Failure -> _state.value = ConsumerEconomyUiState(
                    isLoading = false,
                    errorMessage = result.error.message,
                )
            }
        }
    }
}
