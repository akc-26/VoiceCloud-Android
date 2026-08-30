package app.voicecloud.feature.hosting.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.feature.hosting.data.HostingRepository
import app.voicecloud.feature.hosting.model.*
import app.voicecloud.feature.live.rtc.LiveKitListenerEngine
import app.voicecloud.feature.live.rtc.RtcAudioState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
class HostingViewModel @Inject constructor(
    private val repository: HostingRepository,
    private val rtcEngine: LiveKitListenerEngine,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HostingUiState())
    val state: StateFlow<HostingUiState> = mutableState.asStateFlow()

    private var viewerId: String? = null
    private val sessionGeneration = AtomicLong(0L)
    private var rtcJoinJob: Job? = null
    private var stageRefreshJob: Job? = null

    init {
        viewModelScope.launch {
            rtcEngine.state.collectLatest { rtc ->
                mutableState.value = mutableState.value.copy(
                    rtcConnected = rtc == RtcAudioState.Connected || rtc == RtcAudioState.Reconnected,
                )
            }
        }
        viewModelScope.launch {
            rtcEngine.microphoneEnabled.collectLatest { enabled ->
                mutableState.value = mutableState.value.copy(microphoneEnabled = enabled)
            }
        }
    }

    fun setViewer(id: String?) { viewerId = id?.trim()?.takeIf(String::isNotBlank) }

    fun loadCreatorStudio(creatorId: String? = viewerId) {
        setViewer(creatorId)
        viewModelScope.launch {
            mutateLoading(true)
            try {
                val (profile, eligibility) = repository.hostAccess()
                val approved = profile?.status.equals("APPROVED", true)
                val rooms = if (approved) repository.rooms() else emptyList()
                val schedules = viewerId?.let { id -> if (approved) repository.schedules(id) else emptyList() }.orEmpty()
                mutableState.value = mutableState.value.copy(
                    loading = false,
                    eligibilityChecked = true,
                    hostProfile = profile,
                    eligibility = eligibility,
                    rooms = rooms,
                    schedules = schedules,
                    error = null,
                )
            } catch (error: Throwable) {
                fail(error, "Your Creator Studio Couldn’t Be Loaded. Try Again.")
            }
        }
    }

    fun loadStudio() {
        viewModelScope.launch {
            mutateLoading(true)
            try {
                val (profile, eligibility) = repository.hostAccess()
                val rooms = if (profile?.status.equals("APPROVED", true)) repository.rooms() else emptyList()
                val schedules = viewerId?.let { id -> if (profile?.status.equals("APPROVED", true)) repository.schedules(id) else emptyList() }.orEmpty()
                mutableState.value = mutableState.value.copy(
                    loading = false,
                    eligibilityChecked = true,
                    hostProfile = profile,
                    eligibility = eligibility,
                    rooms = rooms,
                    schedules = schedules,
                    error = null,
                )
            } catch (error: Throwable) {
                fail(error, "Host Studio could not be loaded.")
            }
        }
    }

    fun loadVerification() = viewModelScope.launch {
        mutableState.value = mutableState.value.copy(loading = true, error = null)
        try {
            val (profile, eligibility) = repository.hostAccess()
            val progression = runCatching { repository.hostProgression() }.getOrNull()
            val assets = runCatching { repository.verificationAssets() }.getOrDefault(emptyList())
            mutableState.value = mutableState.value.copy(
                loading = false, eligibilityChecked = true, hostProfile = profile, eligibility = eligibility,
                progression = progression, verificationAssets = assets, error = null,
            )
        } catch (error: Throwable) { fail(error, "Host verification could not be loaded.") }
    }

    fun applyForHost(
        realName: String, bio: String?, country: String?, languages: List<String>, categories: List<String>, experience: String?,
    ) = viewModelScope.launch {
        mutableState.value = mutableState.value.copy(verificationBusy = true, error = null, notice = null)
        runCatching { repository.applyForHost(realName, bio, country, languages, categories, experience) }
            .onSuccess { (profile, eligibility) ->
                mutableState.value = mutableState.value.copy(verificationBusy = false, hostProfile = profile, eligibility = eligibility, notice = "Host application submitted.", error = null)
                loadVerification()
            }
            .onFailure {
                mutableState.value = mutableState.value.copy(verificationBusy = false)
                fail(it, "Host application could not be submitted.")
            }
    }

    fun uploadVerification(
        kind: HostVerificationAssetKind, bytes: ByteArray, fileName: String, mimeType: String,
    ) = viewModelScope.launch {
        mutableState.value = mutableState.value.copy(verificationBusy = true, error = null, notice = null)
        runCatching { repository.uploadVerificationAsset(kind, bytes, fileName, mimeType) }
            .onSuccess { assets -> mutableState.value = mutableState.value.copy(verificationBusy = false, verificationAssets = assets, notice = "Verification file uploaded.", error = null) }
            .onFailure { mutableState.value = mutableState.value.copy(verificationBusy = false); fail(it, "Verification file could not be uploaded.") }
    }

    fun replaceVerification(
        assetId: String, kind: HostVerificationAssetKind, bytes: ByteArray, fileName: String, mimeType: String,
    ) = viewModelScope.launch {
        mutableState.value = mutableState.value.copy(verificationBusy = true, error = null, notice = null)
        runCatching { repository.replaceVerificationAsset(assetId, kind, bytes, fileName, mimeType) }
            .onSuccess { assets -> mutableState.value = mutableState.value.copy(verificationBusy = false, verificationAssets = assets, notice = "Verification file replaced.", error = null) }
            .onFailure { mutableState.value = mutableState.value.copy(verificationBusy = false); fail(it, "Verification file could not be replaced.") }
    }

    fun loadRoom(roomId: String) {
        viewModelScope.launch {
            mutateLoading(true)
            runCatching { repository.room(roomId) }
                .onSuccess { mutableState.value = mutableState.value.copy(loading = false, selectedRoom = it, error = null) }
                .onFailure { fail(it, "Room could not be loaded.") }
        }
    }

    fun createRoom(input: RoomEditorInput, onCreated: (HostRoom) -> Unit) = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.createRoom(input) }
            .onSuccess { room ->
                mutableState.value = mutableState.value.copy(loading = false, selectedRoom = room, rooms = (listOf(room) + mutableState.value.rooms).distinctBy { it.id }, notice = "Room created.", error = null)
                onCreated(room)
            }
            .onFailure { fail(it, "Room could not be created.") }
    }

    fun updateRoom(roomId: String, input: RoomEditorInput, onSaved: (HostRoom) -> Unit = {}) = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.updateRoom(roomId, input) }
            .onSuccess { room ->
                mutableState.value = mutableState.value.copy(loading = false, selectedRoom = room, rooms = mutableState.value.rooms.map { if (it.id == room.id) room else it }, notice = "Room settings saved.", error = null)
                onSaved(room)
            }
            .onFailure { fail(it, "Room settings could not be saved.") }
    }

    fun deleteRoom(roomId: String, onDeleted: () -> Unit) = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.deleteRoom(roomId) }
            .onSuccess {
                mutableState.value = mutableState.value.copy(loading = false, rooms = mutableState.value.rooms.filterNot { it.id == roomId }, selectedRoom = null, notice = "Room deleted.", error = null)
                onDeleted()
            }
            .onFailure { fail(it, "Room could not be deleted.") }
    }

    fun startRoom(roomId: String, onStarted: (HostRoom) -> Unit) = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.startRoom(roomId) }
            .onSuccess { room ->
                updateRoomState(room)
                mutableState.value = mutableState.value.copy(loading = false, notice = "Room is live.", error = null)
                onStarted(room)
            }
            .onFailure { fail(it, "Room could not be started.") }
    }

    fun pauseRoom(roomId: String) = lifecycleMutation(roomId, "Room paused.") { repository.pauseRoom(it) }
    fun resumeRoom(roomId: String) = lifecycleMutation(roomId, "Room resumed.") { repository.resumeRoom(it) }

    fun endRoom(roomId: String, onEnded: () -> Unit = {}) = viewModelScope.launch {
        runCatching { rtcEngine.setMicrophoneEnabled(false) }
        rtcEngine.disconnect()
        stopStageRefresh()
        runCatching { repository.leaveHostRtc(roomId) }
        runCatching { repository.endRoom(roomId) }
            .onSuccess { room ->
                updateRoomState(room)
                mutableState.value = mutableState.value.copy(notice = "Room ended.", rtcConnected = false, microphoneEnabled = false, stage = null, error = null)
                onEnded()
            }
            .onFailure { fail(it, "Room could not be ended.") }
    }

    fun enterHostConsole(roomId: String) {
        val generation = sessionGeneration.incrementAndGet()
        rtcJoinJob?.cancel()
        rtcJoinJob = viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, error = null)
            try {
                val room = repository.room(roomId)
                if (sessionGeneration.get() != generation) return@launch
                mutableState.value = mutableState.value.copy(selectedRoom = room)
                val join = repository.joinHostRtc(roomId)
                if (!isCurrent(generation, roomId)) return@launch
                val url = join.serverUrl?.takeIf(String::isNotBlank) ?: error("Live audio is unavailable.")
                if (!join.provider.equals("livekit", true)) error("Live audio is unavailable.")
                rtcEngine.connect(url, join.token)
                if (!isCurrent(generation, roomId)) {
                    rtcEngine.disconnect()
                    throw CancellationException("Host session superseded")
                }
                mutableState.value = mutableState.value.copy(loading = false, selectedRoom = room, error = null)
                startStageRefresh(roomId, generation)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                fail(error, "Live host controls could not connect.")
            }
        }
    }

    fun leaveHostConsole(roomId: String) {
        sessionGeneration.incrementAndGet()
        rtcJoinJob?.cancel()
        stopStageRefresh()
        viewModelScope.launch {
            runCatching { rtcEngine.setMicrophoneEnabled(false) }
            rtcEngine.disconnect()
            runCatching { repository.leaveHostRtc(roomId) }
            mutableState.value = mutableState.value.copy(rtcConnected = false, microphoneEnabled = false, microphoneBusy = false, stage = null)
        }
    }

    fun setMicrophoneEnabled(enabled: Boolean) = viewModelScope.launch {
        mutableState.value = mutableState.value.copy(microphoneBusy = true, error = null)
        try {
            val changed = rtcEngine.setMicrophoneEnabled(enabled)
            if (!changed && enabled) error("Microphone could not be enabled.")
            mutableState.value = mutableState.value.copy(microphoneBusy = false, microphoneEnabled = enabled && changed, notice = if (enabled) "Microphone is live." else "Microphone muted.")
        } catch (error: SecurityException) {
            mutableState.value = mutableState.value.copy(microphoneBusy = false, microphoneEnabled = false, error = "Microphone permission is required before you can speak.")
        } catch (error: Throwable) {
            fail(error, "Microphone state could not be changed.")
            mutableState.value = mutableState.value.copy(microphoneBusy = false)
        }
    }

    fun loadSchedule(scheduleId: String) = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.schedule(scheduleId) }
            .onSuccess { mutableState.value = mutableState.value.copy(loading = false, selectedSchedule = it, error = null) }
            .onFailure { fail(it, "Scheduled room could not be loaded.") }
    }

    fun createSchedule(input: ScheduledRoomInput, onCreated: (ScheduledHostRoom) -> Unit) = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.createSchedule(input) }
            .onSuccess { schedule ->
                mutableState.value = mutableState.value.copy(loading = false, selectedSchedule = schedule, schedules = (mutableState.value.schedules + schedule).distinctBy { it.id }, notice = "Room scheduled.", error = null)
                onCreated(schedule)
            }
            .onFailure { fail(it, "Room could not be scheduled.") }
    }

    fun updateSchedule(scheduleId: String, input: ScheduledRoomInput, onSaved: (ScheduledHostRoom) -> Unit = {}) = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.updateSchedule(scheduleId, input) }
            .onSuccess { schedule ->
                mutableState.value = mutableState.value.copy(loading = false, selectedSchedule = schedule, schedules = mutableState.value.schedules.map { if (it.id == schedule.id) schedule else it }, notice = "Schedule updated.", error = null)
                onSaved(schedule)
            }
            .onFailure { fail(it, "Schedule could not be updated.") }
    }

    fun deleteSchedule(scheduleId: String, onDeleted: () -> Unit = {}) = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.deleteSchedule(scheduleId) }
            .onSuccess {
                mutableState.value = mutableState.value.copy(loading = false, schedules = mutableState.value.schedules.filterNot { it.id == scheduleId }, selectedSchedule = null, notice = "Schedule deleted.", error = null)
                onDeleted()
            }
            .onFailure { fail(it, "Schedule could not be deleted.") }
    }

    fun startScheduled(schedule: ScheduledHostRoom, onStarted: (HostRoom) -> Unit) = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.startScheduled(schedule) }
            .onSuccess { room ->
                updateRoomState(room)
                mutableState.value = mutableState.value.copy(loading = false, notice = "Scheduled room is live.", error = null)
                onStarted(room)
            }
            .onFailure { fail(it, "Scheduled room could not be started.") }
    }

    fun refreshStage(roomId: String) = viewModelScope.launch { refreshStageNow(roomId) }
    fun approve(roomId: String, userId: String, seat: Int? = null) = stageAction(roomId) { repository.approve(roomId, userId, seat) }
    fun reject(roomId: String, userId: String) = stageAction(roomId) { repository.reject(roomId, userId) }
    fun inviteSpeaker(roomId: String, userId: String) = stageAction(roomId) { repository.inviteSpeaker(roomId, userId) }
    fun removeSpeaker(roomId: String, userId: String) = stageAction(roomId) { repository.removeSpeaker(roomId, userId) }
    fun muteSpeaker(roomId: String, userId: String, muted: Boolean) = stageAction(roomId) { repository.mute(roomId, userId, muted) }
    fun lockSeat(roomId: String, seat: Int, locked: Boolean) = stageAction(roomId) { repository.lockSeat(roomId, seat, locked) }
    fun searchInviteCandidates(query: String) = viewModelScope.launch {
        val normalized = query.trim()
        mutableState.value = mutableState.value.copy(inviteQuery = normalized)
        if (normalized.length < 2) {
            mutableState.value = mutableState.value.copy(inviteCandidates = emptyList())
            return@launch
        }
        runCatching { repository.searchInviteCandidates(normalized, viewerId) }
            .onSuccess { mutableState.value = mutableState.value.copy(inviteCandidates = it, error = null) }
            .onFailure { fail(it, "People search is temporarily unavailable.") }
    }

    fun inviteParticipant(roomId: String, userId: String) = viewModelScope.launch {
        runCatching { repository.inviteParticipant(roomId, userId) }
            .onSuccess {
                mutableState.value = mutableState.value.copy(
                    inviteCandidates = mutableState.value.inviteCandidates.filterNot { it.id == userId },
                    notice = "Invitation sent.", error = null,
                )
            }
            .onFailure { fail(it, "Invitation could not be sent.") }
    }

    fun loadInteractive(roomId: String) = viewModelScope.launch {
        val polls = runCatching { repository.polls(roomId) }.getOrDefault(emptyList())
        val quiz = repository.activeQuiz(roomId)
        mutableState.value = mutableState.value.copy(polls = polls, activeQuiz = quiz)
    }

    fun createPoll(body: CreatePollBody) = viewModelScope.launch {
        runCatching { repository.createPoll(body) }
            .onSuccess { mutableState.value = mutableState.value.copy(polls = (listOf(it) + mutableState.value.polls).distinctBy { p -> p.id }, notice = "Poll created.", error = null) }
            .onFailure { fail(it, "Poll could not be created.") }
    }
    fun startPoll(id: String, roomId: String) = interactiveAction(roomId, "Poll started.") { repository.startPoll(id) }
    fun stopPoll(id: String, roomId: String) = interactiveAction(roomId, "Poll ended.") { repository.stopPoll(id) }
    fun deletePoll(id: String, roomId: String) = viewModelScope.launch {
        runCatching { repository.deletePoll(id) }
            .onSuccess { mutableState.value = mutableState.value.copy(polls = mutableState.value.polls.filterNot { it.id == id }, notice = "Poll deleted.", error = null) }
            .onFailure { fail(it, "Poll could not be deleted.") }
        loadInteractive(roomId)
    }

    fun createQuiz(body: CreateQuizBody) = viewModelScope.launch {
        runCatching { repository.createQuiz(body) }
            .onSuccess { mutableState.value = mutableState.value.copy(activeQuiz = it, notice = "Quiz created.", error = null) }
            .onFailure { fail(it, "Quiz could not be created.") }
    }
    fun startQuiz(id: String, roomId: String) = quizAction(roomId, "Quiz started.") { repository.startQuiz(id) }
    fun nextQuizRound(id: String, roomId: String) = quizAction(roomId, "Next quiz round started.") { repository.nextQuizRound(id) }
    fun stopQuiz(id: String, roomId: String) = quizAction(roomId, "Quiz ended.") { repository.stopQuiz(id) }

    fun clearNotice() { mutableState.value = mutableState.value.copy(notice = null, error = null) }

    private fun lifecycleMutation(roomId: String, notice: String, action: suspend (String) -> HostRoom) = viewModelScope.launch {
        runCatching { action(roomId) }
            .onSuccess { updateRoomState(it); mutableState.value = mutableState.value.copy(notice = notice, error = null) }
            .onFailure { fail(it, "Room state could not be changed.") }
    }

    private fun stageAction(roomId: String, action: suspend () -> Any?) = viewModelScope.launch {
        runCatching { action() }
            .onSuccess { refreshStageNow(roomId) }
            .onFailure { fail(it, "Stage action could not be completed.") }
    }

    private fun interactiveAction(roomId: String, notice: String, action: suspend () -> Any?) = viewModelScope.launch {
        runCatching { action() }
            .onSuccess { mutableState.value = mutableState.value.copy(notice = notice, error = null); loadInteractive(roomId) }
            .onFailure { fail(it, "Poll action could not be completed.") }
    }

    private fun quizAction(roomId: String, notice: String, action: suspend () -> RoomQuiz) = viewModelScope.launch {
        runCatching { action() }
            .onSuccess { mutableState.value = mutableState.value.copy(activeQuiz = it, notice = notice, error = null); loadInteractive(roomId) }
            .onFailure { fail(it, "Quiz action could not be completed.") }
    }

    private fun startStageRefresh(roomId: String, generation: Long) {
        stopStageRefresh()
        stageRefreshJob = viewModelScope.launch {
            while (isCurrent(generation, roomId)) {
                refreshStageNow(roomId)
                delay(3_000)
            }
        }
    }

    private suspend fun refreshStageNow(roomId: String) {
        runCatching { repository.stage(roomId) }
            .onSuccess { stage -> if (mutableState.value.selectedRoom?.id == roomId) mutableState.value = mutableState.value.copy(stage = stage) }
    }

    private fun stopStageRefresh() { stageRefreshJob?.cancel(); stageRefreshJob = null }
    private fun isCurrent(generation: Long, roomId: String) = sessionGeneration.get() == generation && mutableState.value.selectedRoom?.id == roomId

    private fun updateRoomState(room: HostRoom) {
        mutableState.value = mutableState.value.copy(
            selectedRoom = room,
            rooms = (mutableState.value.rooms.filterNot { it.id == room.id } + room).sortedByDescending { it.createdAt.orEmpty() },
        )
    }

    private fun mutateLoading(loading: Boolean) { mutableState.value = mutableState.value.copy(loading = loading, error = null) }
    private fun fail(error: Throwable, fallback: String) {
        mutableState.value = mutableState.value.copy(loading = false, error = repository.userFacingError(error, fallback))
    }

    override fun onCleared() {
        sessionGeneration.incrementAndGet()
        rtcJoinJob?.cancel()
        stopStageRefresh()
        rtcEngine.disconnect()
        super.onCleared()
    }
}
