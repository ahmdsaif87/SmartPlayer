package id.smartplayer.di

import android.content.Context
import androidx.room.Room
import id.smartplayer.data.db.AppDatabase
import id.smartplayer.data.parse.DefaultFilenameParser
import id.smartplayer.data.repo.LibraryRepository
import id.smartplayer.data.repo.PlaybackRepository
import id.smartplayer.data.repo.SettingsRepository
import id.smartplayer.data.scan.MediaStoreScanner
import id.smartplayer.data.scan.ScanCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(private val context: Context) {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "smartplayer.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val scanner: MediaStoreScanner by lazy {
        MediaStoreScanner(context)
    }

    val parser: DefaultFilenameParser by lazy {
        DefaultFilenameParser()
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    val scanCoordinator: ScanCoordinator by lazy {
        ScanCoordinator(
            scanner = scanner,
            database = database,
            parser = parser,
            externalScope = applicationScope
        )
    }

    val libraryRepository: LibraryRepository by lazy {
        LibraryRepository(database, scanCoordinator)
    }

    val playbackRepository: PlaybackRepository by lazy {
        PlaybackRepository(database.playbackStateDao())
    }
}
