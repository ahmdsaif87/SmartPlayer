package id.smartplayer.di

import android.content.Context
import id.smartplayer.data.ai.GeminiAiClient
import id.smartplayer.data.ai.MetadataEnricher
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

/**
 * Manual Dependency Injection container held by [id.smartplayer.SmartPlayerApp].
 * Avoids reflection/annotation-processor overhead of Hilt/Dagger, keeping APK size minimal.
 */
class AppContainer(private val context: Context) {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
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

    val aiService: GeminiAiClient by lazy {
        GeminiAiClient { settingsRepository.apiKey.value }
    }

    val metadataEnricher: MetadataEnricher by lazy {
        MetadataEnricher(
            context = context,
            database = database,
            aiService = aiService,
            settingsRepository = settingsRepository,
            scope = applicationScope
        )
    }

    val scanCoordinator: ScanCoordinator by lazy {
        ScanCoordinator(
            scanner = scanner,
            database = database,
            parser = parser,
            externalScope = applicationScope
        ).apply {
            onNewSeriesDiscovered = { newSeriesIds ->
                metadataEnricher.enrichSeriesAsync(newSeriesIds)
            }
        }
    }

    val libraryRepository: LibraryRepository by lazy {
        LibraryRepository(database, scanCoordinator)
    }

    val playbackRepository: PlaybackRepository by lazy {
        PlaybackRepository(database.playbackStateDao())
    }
}
