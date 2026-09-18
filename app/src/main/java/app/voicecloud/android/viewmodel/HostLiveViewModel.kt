package app.voicecloud.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.android.ui.live.HostLiveUiState
import app.voicecloud.android.ui.live.HostSessionStatus
import app.voicecloud.core.data.LiveRoomRepository
import app.voicecloud.core.model.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostLiveViewModel @Inject constructor(
    private val liveRoomRepository: LiveRoomRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HostLiveUiState())
    val state: StateFlow<HostLiveUiState> = _state.asStateFlow()
    private var activeRoomId: String? = null

    fun joinRoom(roomId: String) {
        if (roomId.isBlank()) return
        activeRoomId = roomId
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, roomTitle = null)
            when (val result = liveRoomRepository.joinRoom(roomId)) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    sessionStatus = HostSessionStatus.Live,
                    roomTitle = roomId,
                )
                is ApiResult.Failure -> _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = result.error.message,
                )
            }
        }
    }

    fun leaveRoom() {
        activeRoomId?.let { liveRoomRepository.leaveRoom(it) }
        activeRoomId = null
        _state.value = HostLiveUiState()
    }
}
