package app.voicecloud.core.data

import app.voicecloud.core.data.mapper.toPersonUiModel
import app.voicecloud.core.data.mapper.toRoomUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.model.ApiResult
import app.voicecloud.core.network.SafeApiErrorParser
import app.voicecloud.core.network.SearchApi
import app.voicecloud.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

data class SearchResults(
    val rooms: List<VCRoomUiModel> = emptyList(),
    val people: List<VCPersonUiModel> = emptyList(),
)

@Singleton
class SearchRepository @Inject constructor(
    private val api: SearchApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun search(query: String): ApiResult<SearchResults> {
        if (query.isBlank()) return ApiResult.Success(SearchResults())
        return when (val result = apiCall({ api.search(query) }, parser)) {
            is ApiResult.Success -> {
                val envelope = result.data.data
                ApiResult.Success(
                    SearchResults(
                        rooms = envelope?.rooms.orEmpty().mapNotNull { it.toRoomUiModel() },
                        people = envelope?.users.orEmpty().mapNotNull { it.toPersonUiModel() }
                            .ifEmpty { envelope?.hosts.orEmpty().mapNotNull { it.toPersonUiModel() } },
                    ),
                )
            }
            is ApiResult.Failure -> result
        }
    }
}
