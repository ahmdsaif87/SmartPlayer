package id.smartplayer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import id.smartplayer.data.db.entities.EpisodeEntity
import id.smartplayer.data.db.entities.PlaybackStateEntity
import id.smartplayer.data.db.entities.SeriesEntity

@Database(
    entities = [
        SeriesEntity::class,
        EpisodeEntity::class,
        PlaybackStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun seriesDao(): SeriesDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun playbackStateDao(): PlaybackStateDao

    companion object {
        private const val DB_NAME = "smartplayer.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
