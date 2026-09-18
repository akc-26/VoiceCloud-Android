package app.voicecloud.core.data

import app.voicecloud.core.data.mapper.toPersonUiModel
import app.voicecloud.core.data.mapper.toRoomUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.model.ApiResult
import app.voicecloud.core.network.DiscoveryApi
import app.voicecloud.core.network.SafeApiErrorParser
import app.voicecloud.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoveryRepository @Inject constructor(
    private val api: DiscoveryApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun liveRooms(): ApiResult<List<VCRoomUiModel>> = loadRooms { api.liveRooms() }

    suspend fun trendingRooms(): ApiResult<List<VCRoomUiModel>> = loadRooms { api.trendingRooms() }

    suspend fun popularRooms(): ApiResult<List<VCRoomUiModel>> = loadRooms { api.popularRooms() }

    suspend fun popularPeople(): ApiResult<List<VCPersonUiModel>> = loadPeople { api.popularUsers() }

    suspend fun onlinePeople(): ApiResult<List<VCPersonUiModel>> = loadPeople { api.onlineUsers() }

    suspend fun suggestedPeople(): ApiResult<List<VCPersonUiModel>> = loadPeople { api.suggestedUsers() }

    suspend fun trendingPeople(): ApiResult<List<VCPersonUiModel>> = loadPeople { api.trendingUsers() }

    suspend fun trendingHosts(): ApiResult<List<VCPersonUiModel>> = loadPeople { api.trendingHosts() }

    private suspend fun loadRooms(
        call: suspend () -> retrofit2.Response<app.voicecloud.core.model.DataEnvelope<List<app.voicecloud.core.model.DiscoveryRoomDto>>>,
    ): ApiResult<List<VCRoomUiModel>> = when (val result = apiCall(call, parser)) {
        is ApiResult.Success -> ApiResult.Success(
            result.data.data.orEmpty().mapNotNull { it.toRoomUiModel() },
        )
        is ApiResult.Failure -> result
    }

    private suspend fun loadPeople(
        call: suspend () -> retrofit2.Response<app.voicecloud.core.model.DataEnvelope<List<app.voicecloud.core.model.DiscoveryUserDto>>>,
    ): ApiResult<List<VCPersonUiModel>> = when (val result = apiCall(call, parser)) {
        is ApiResult.Success -> ApiResult.Success(
            result.data.data.orEmpty().mapNotNull { it.toPersonUiModel() },
        )
        is ApiResult.Failure -> result
    }
}
