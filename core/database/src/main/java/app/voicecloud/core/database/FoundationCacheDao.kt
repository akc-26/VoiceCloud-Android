package app.voicecloud.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FoundationCacheDao {
    @Query("SELECT * FROM foundation_cache WHERE `key` = :key LIMIT 1") fun observe(key: String): Flow<FoundationCacheEntity?>
    @Upsert suspend fun upsert(item: FoundationCacheEntity)
    @Query("DELETE FROM foundation_cache") suspend fun clear()
}
