package app.voicecloud.core.data

import app.voicecloud.core.data.mapper.toRoomUiModel
import app.voicecloud.core.data.mapper.toBalanceLabel
import app.voicecloud.core.data.mapper.toLineLabel
import app.voicecloud.core.model.ApiResult
import app.voicecloud.core.model.RoomDetailDto
import app.voicecloud.core.model.RtcJoinRequest
import app.voicecloud.core.model.RtcJoinResponse
import app.voicecloud.core.network.RoomsApi
import app.voicecloud.core.network.RtcApi
import app.voicecloud.core.network.SafeApiErrorParser
import app.voicecloud.core.network.WalletApi
import app.voicecloud.core.network.apiCall
import app.voicecloud.core.realtime.RealtimeClient
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class WalletOverview(
    val balanceLabel: String,
    val transactions: List<String> = emptyList(),
)

@Singleton
class WalletRepository @Inject constructor(
    private val api: WalletApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun overview(): ApiResult<WalletOverview> {
        val balanceResult = apiCall({ api.balance() }, parser)
        if (balanceResult is ApiResult.Failure) return balanceResult
        val balance = (balanceResult as ApiResult.Success).data.data
        val historyResult = apiCall({ api.history() }, parser)
        val lines = if (historyResult is ApiResult.Success) {
            historyResult.data.data.orEmpty().map { it.toLineLabel() }
        } else {
            emptyList()
        }
        return ApiResult.Success(
            WalletOverview(
                balanceLabel = balance?.toBalanceLabel() ?: "—",
                transactions = lines,
            ),
        )
    }
}

@Singleton
class RoomRepository @Inject constructor(
    private val api: RoomsApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun loadRoom(roomId: String): ApiResult<RoomDetailDto> = when (val result = apiCall({ api.room(roomId) }, parser)) {
        is ApiResult.Success -> {
            val dto = result.data.data
            if (dto == null) ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Room not found."))
            else ApiResult.Success(dto)
        }
        is ApiResult.Failure -> result
    }

    suspend fun savedRooms(): ApiResult<List<app.voicecloud.core.designsystem.component.VCRoomUiModel>> =
        when (val result = apiCall({ api.savedRooms() }, parser)) {
            is ApiResult.Success -> ApiResult.Success(
                result.data.data.orEmpty().mapNotNull { it.toRoomUiModel() },
            )
            is ApiResult.Failure -> result
        }
}

@Singleton
class LiveRoomRepository @Inject constructor(
    private val rtcApi: RtcApi,
    private val realtimeClient: RealtimeClient,
    private val parser: SafeApiErrorParser,
) {
    suspend fun joinRoom(roomId: String): ApiResult<RtcJoinResponse> {
        val rtc = when (val result = apiCall({ rtcApi.joinRoom(RtcJoinRequest(roomId)) }, parser)) {
            is ApiResult.Success -> result.data.data
            is ApiResult.Failure -> return result
        }
        if (rtc == null) {
            return ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Live room join failed."))
        }
        if (!realtimeClient.connect()) {
            return ApiResult.Failure(
                app.voicecloud.core.model.ApiError(message = "Realtime connection failed.", retryable = true),
            )
        }
        realtimeClient.emit(
            "join_room",
            JSONObject().put("roomId", roomId),
        )
        return ApiResult.Success(rtc)
    }

    fun leaveRoom(roomId: String) {
        realtimeClient.emit("leave_room", JSONObject().put("roomId", roomId))
    }
}
