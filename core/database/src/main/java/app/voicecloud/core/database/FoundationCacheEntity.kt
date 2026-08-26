package app.voicecloud.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foundation_cache")
data class FoundationCacheEntity(@PrimaryKey val key: String, val value: String, val updatedAtEpochMs: Long)
