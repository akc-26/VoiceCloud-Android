package app.voicecloud.core.data

import app.voicecloud.core.data.mapper.toPersonUiModel
import app.voicecloud.core.data.mapper.toRoomUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.model.AchievementItemDto
import app.voicecloud.core.model.ApiResult
import app.voicecloud.core.model.FollowStatsDto
import app.voicecloud.core.model.ReferralSummaryDto
import app.voicecloud.core.model.TaskItemDto
import app.voicecloud.core.model.UserProfileDto
import app.voicecloud.core.model.VoiceCloudUser
import app.voicecloud.core.network.BlocksApi
import app.voicecloud.core.network.RankingsApi
import app.voicecloud.core.network.ReferralsApi
import app.voicecloud.core.network.SafeApiErrorParser
import app.voicecloud.core.network.TasksAchievementsApi
import app.voicecloud.core.network.UsersApi
import app.voicecloud.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

data class PublicProfileData(
    val user: VoiceCloudUser,
    val followerCount: Int?,
    val followingCount: Int?,
    val isFollowing: Boolean,
)

@Singleton
class PublicProfileRepository @Inject constructor(
    private val usersApi: UsersApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun load(userId: String): ApiResult<PublicProfileData> {
        val profileResult = apiCall({ usersApi.profile(userId) }, parser)
        if (profileResult is ApiResult.Failure) return profileResult
        val profile = (profileResult as ApiResult.Success).data.data ?: return ApiResult.Failure(
            app.voicecloud.core.model.ApiError(message = "Profile not found."),
        )
        val stats = when (val statsResult = apiCall({ usersApi.followStats(userId) }, parser)) {
            is ApiResult.Success -> statsResult.data.data
            is ApiResult.Failure -> null
        }
        return ApiResult.Success(profile.toPublicProfile(stats))
    }

    private fun UserProfileDto.toPublicProfile(stats: FollowStatsDto?): PublicProfileData =
        PublicProfileData(
            user = VoiceCloudUser(
                id = resolvedId.orEmpty(),
                username = username,
                displayName = displayName,
                bio = bio,
                avatarUrl = avatarUrl,
                role = role,
            ),
            followerCount = stats?.followerCount ?: followerCount,
            followingCount = stats?.followingCount ?: followingCount,
            isFollowing = stats?.isFollowing == true,
        )
}

@Singleton
class BlocksRepository @Inject constructor(
    private val api: BlocksApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun blockedUsers(): ApiResult<List<VCPersonUiModel>> = loadPeople { api.blockedUsers() }

    suspend fun block(userId: String): ApiResult<Unit> = when (val result = apiCall({ api.block(userId) }, parser)) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }

    suspend fun unblock(userId: String): ApiResult<Unit> = when (val result = apiCall({ api.unblock(userId) }, parser)) {
        is ApiResult.Success -> ApiResult.Success(Unit)
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
class RankingsRepository @Inject constructor(
    private val api: RankingsApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun leaderboardUsers(): ApiResult<List<VCPersonUiModel>> = loadPeople { api.userLeaderboard() }

    suspend fun trendingUsers(): ApiResult<List<VCPersonUiModel>> = loadPeople { api.trendingUsers() }

    private suspend fun loadPeople(
        call: suspend () -> retrofit2.Response<app.voicecloud.core.model.DataEnvelope<List<app.voicecloud.core.model.DiscoveryUserDto>>>,
    ): ApiResult<List<VCPersonUiModel>> = when (val result = apiCall(call, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty().mapNotNull { it.toPersonUiModel() })
        is ApiResult.Failure -> result
    }
}

@Singleton
class ReferralsRepository @Inject constructor(
    private val api: ReferralsApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun summary(): ApiResult<ReferralSummaryDto> = when (val result = apiCall({ api.summary() }, parser)) {
        is ApiResult.Success -> {
            val body = result.data.data
            if (body == null) ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Referrals unavailable."))
            else ApiResult.Success(body)
        }
        is ApiResult.Failure -> result
    }
}

@Singleton
class TasksAchievementsRepository @Inject constructor(
    private val api: TasksAchievementsApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun tasks(): ApiResult<List<TaskItemDto>> = when (val result = apiCall({ api.tasks() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty())
        is ApiResult.Failure -> result
    }

    suspend fun achievements(): ApiResult<List<AchievementItemDto>> = when (val result = apiCall({ api.achievements() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty())
        is ApiResult.Failure -> result
    }
}

@Singleton
class SavedRoomsRepository @Inject constructor(
    private val roomRepository: RoomRepository,
) {
    suspend fun load(): ApiResult<List<VCRoomUiModel>> = roomRepository.savedRooms()
}
