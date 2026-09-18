package app.voicecloud.core.data

import app.voicecloud.core.data.mapper.toCommunityUiModel
import app.voicecloud.core.data.mapper.toEventUiModel
import app.voicecloud.core.data.mapper.toPersonUiModel
import app.voicecloud.core.designsystem.component.VCCommunityUiModel
import app.voicecloud.core.designsystem.component.VCEventUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.model.ApiResult
import app.voicecloud.core.model.ClubDto
import app.voicecloud.core.model.CreateClubRequest
import app.voicecloud.core.model.FriendRequestBody
import app.voicecloud.core.model.FriendRequestDto
import app.voicecloud.core.model.ProfileVisitorDto
import app.voicecloud.core.model.RoomActivityItemDto
import app.voicecloud.core.model.ScheduledRoomDto
import app.voicecloud.core.model.UpdateClubRequest
import app.voicecloud.core.model.UpdateUserSettingsRequest
import app.voicecloud.core.model.UserSettingsDto
import app.voicecloud.core.network.ClubsApi
import app.voicecloud.core.network.FriendsApi
import app.voicecloud.core.network.RoomActivityApi
import app.voicecloud.core.network.SafeApiErrorParser
import app.voicecloud.core.network.ScheduledRoomsApi
import app.voicecloud.core.network.UserSettingsApi
import app.voicecloud.core.network.VisitorsApi
import app.voicecloud.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

data class FriendRequestItem(
    val id: String,
    val person: VCPersonUiModel,
)

data class ActivityHistoryItem(
    val id: String,
    val title: String,
    val whenLabel: String?,
)

@Singleton
class FriendsRepository @Inject constructor(
    private val api: FriendsApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun friends(): ApiResult<List<VCPersonUiModel>> = loadPeople { api.friends() }

    suspend fun suggested(): ApiResult<List<VCPersonUiModel>> = loadPeople { api.suggested() }

    suspend fun pendingRequests(): ApiResult<List<FriendRequestItem>> = when (val result = apiCall({ api.pendingRequests() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(
            result.data.data.orEmpty().mapNotNull { it.toRequestItem() },
        )
        is ApiResult.Failure -> result
    }

    suspend fun sendRequest(userId: String): ApiResult<Unit> = when (
        val result = apiCall({ api.sendRequest(FriendRequestBody(userId)) }, parser)
    ) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }

    suspend fun accept(requestId: String): ApiResult<Unit> = action { api.acceptRequest(requestId) }

    suspend fun reject(requestId: String): ApiResult<Unit> = action { api.rejectRequest(requestId) }

    suspend fun removeFriend(userId: String): ApiResult<Unit> = action { api.removeFriend(userId) }

    private suspend fun action(call: suspend () -> retrofit2.Response<Unit>): ApiResult<Unit> = when (val result = apiCall(call, parser)) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }

    private suspend fun loadPeople(
        call: suspend () -> retrofit2.Response<app.voicecloud.core.model.DataEnvelope<List<app.voicecloud.core.model.DiscoveryUserDto>>>,
    ): ApiResult<List<VCPersonUiModel>> = when (val result = apiCall(call, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty().mapNotNull { it.toPersonUiModel() })
        is ApiResult.Failure -> result
    }

    private fun FriendRequestDto.toRequestItem(): FriendRequestItem? {
        val requestId = resolvedId ?: return null
        val person = (user ?: fromUser)?.toPersonUiModel() ?: return null
        return FriendRequestItem(id = requestId, person = person)
    }
}

@Singleton
class VisitorsRepository @Inject constructor(
    private val api: VisitorsApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun visitors(): ApiResult<List<VCPersonUiModel>> = when (val result = apiCall({ api.visitors() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(
            result.data.data.orEmpty().mapNotNull { it.toPersonRow() },
        )
        is ApiResult.Failure -> result
    }

    suspend fun statsTotal(): ApiResult<Int> = when (val result = apiCall({ api.stats() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data?.resolvedTotal ?: 0)
        is ApiResult.Failure -> result
    }

    suspend fun recordVisit(userId: String): ApiResult<Unit> = when (val result = apiCall({ api.recordVisit(userId) }, parser)) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }

    private fun ProfileVisitorDto.toPersonRow(): VCPersonUiModel? {
        val person = resolvedPerson?.toPersonUiModel() ?: return null
        return person.copy(metaLabel = resolvedVisitedAt ?: person.metaLabel)
    }
}

@Singleton
class ActivityHistoryRepository @Inject constructor(
    private val api: RoomActivityApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun load(): ApiResult<List<ActivityHistoryItem>> = when (val result = apiCall({ api.myActivity() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty().mapNotNull { it.toItem() })
        is ApiResult.Failure -> result
    }

    private fun RoomActivityItemDto.toItem(): ActivityHistoryItem? {
        val activityId = id ?: roomId ?: return null
        return ActivityHistoryItem(
            id = activityId,
            title = resolvedTitle,
            whenLabel = resolvedWhen,
        )
    }
}

@Singleton
class UserSettingsRepository @Inject constructor(
    private val api: UserSettingsApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun load(): ApiResult<UserSettingsDto> = when (val result = apiCall({ api.settings() }, parser)) {
        is ApiResult.Success -> {
            val body = result.data.data
            if (body == null) ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Settings unavailable."))
            else ApiResult.Success(body)
        }
        is ApiResult.Failure -> result
    }

    suspend fun update(request: UpdateUserSettingsRequest): ApiResult<UserSettingsDto> = when (
        val result = apiCall({ api.updateSettings(request) }, parser)
    ) {
        is ApiResult.Success -> {
            val body = result.data.data
            if (body == null) ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Could not save settings."))
            else ApiResult.Success(body)
        }
        is ApiResult.Failure -> result
    }
}

@Singleton
class ClubsRepository @Inject constructor(
    private val api: ClubsApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun list(): ApiResult<List<VCCommunityUiModel>> = loadCommunities { api.clubs() }

    suspend fun detail(clubId: String): ApiResult<VCCommunityUiModel> = when (val result = apiCall({ api.club(clubId) }, parser)) {
        is ApiResult.Success -> {
            val dto = result.data.data
            if (dto == null) ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Community not found."))
            else ApiResult.Success(dto.toCommunityUiModel())
        }
        is ApiResult.Failure -> result
    }

    suspend fun create(name: String, description: String?): ApiResult<VCCommunityUiModel> = when (
        val result = apiCall({ api.createClub(CreateClubRequest(name = name, description = description)) }, parser)
    ) {
        is ApiResult.Success -> {
            val dto = result.data.data
            if (dto == null) ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Could not create community."))
            else ApiResult.Success(dto.toCommunityUiModel())
        }
        is ApiResult.Failure -> result
    }

    suspend fun join(clubId: String): ApiResult<Unit> = action { api.join(clubId) }

    suspend fun leave(clubId: String): ApiResult<Unit> = action { api.leave(clubId) }

    suspend fun members(clubId: String): ApiResult<List<VCPersonUiModel>> = loadPeople { api.members(clubId) }

    suspend fun rooms(clubId: String): ApiResult<List<VCEventUiModel>> = when (val result = apiCall({ api.scheduledRooms(clubId) }, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty().map { it.toEventUiModel() })
        is ApiResult.Failure -> result
    }

    suspend fun update(clubId: String, name: String?, description: String?): ApiResult<VCCommunityUiModel> = when (
        val result = apiCall({ api.updateClub(clubId, UpdateClubRequest(name = name, description = description)) }, parser)
    ) {
        is ApiResult.Success -> {
            val dto = result.data.data
            if (dto == null) ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Update failed."))
            else ApiResult.Success(dto.toCommunityUiModel())
        }
        is ApiResult.Failure -> result
    }

    private suspend fun action(call: suspend () -> retrofit2.Response<Unit>): ApiResult<Unit> = when (val result = apiCall(call, parser)) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }

    private suspend fun loadCommunities(
        call: suspend () -> retrofit2.Response<app.voicecloud.core.model.DataEnvelope<List<ClubDto>>>,
    ): ApiResult<List<VCCommunityUiModel>> = when (val result = apiCall(call, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty().map { it.toCommunityUiModel() })
        is ApiResult.Failure -> result
    }

    private suspend fun loadPeople(
        call: suspend () -> retrofit2.Response<app.voicecloud.core.model.DataEnvelope<List<app.voicecloud.core.model.DiscoveryUserDto>>>,
    ): ApiResult<List<VCPersonUiModel>> = when (val result = apiCall(call, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty().mapNotNull { it.toPersonUiModel() })
        is ApiResult.Failure -> result
    }
}

@Singleton
class EventsRepository @Inject constructor(
    private val api: ScheduledRoomsApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun list(): ApiResult<List<VCEventUiModel>> = when (val result = apiCall({ api.events() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty().map { it.toEventUiModel() })
        is ApiResult.Failure -> result
    }

    suspend fun detail(eventId: String): ApiResult<VCEventUiModel> = when (val result = apiCall({ api.event(eventId) }, parser)) {
        is ApiResult.Success -> {
            val dto = result.data.data
            if (dto == null) ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Event not found."))
            else ApiResult.Success(dto.toEventUiModel())
        }
        is ApiResult.Failure -> result
    }

    suspend fun detailRaw(eventId: String): ApiResult<ScheduledRoomDto> = when (val result = apiCall({ api.event(eventId) }, parser)) {
        is ApiResult.Success -> {
            val dto = result.data.data
            if (dto == null) ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Event not found."))
            else ApiResult.Success(dto)
        }
        is ApiResult.Failure -> result
    }
}
