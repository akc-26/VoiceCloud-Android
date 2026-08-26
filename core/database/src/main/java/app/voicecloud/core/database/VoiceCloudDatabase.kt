package app.voicecloud.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FoundationCacheEntity::class], version = 1, exportSchema = true)
abstract class VoiceCloudDatabase : RoomDatabase() {
    abstract fun foundationCacheDao(): FoundationCacheDao
    companion object { fun create(context: Context): VoiceCloudDatabase = Room.databaseBuilder(context, VoiceCloudDatabase::class.java, "voicecloud.db").build() }
}
