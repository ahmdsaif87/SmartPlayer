package id.smartplayer.ui.settings

import androidx.lifecycle.ViewModel
import id.smartplayer.data.ai.EnrichState
import id.smartplayer.data.ai.MetadataEnricher
import id.smartplayer.data.repo.LibraryRepository
import id.smartplayer.data.repo.SettingsRepository
import id.smartplayer.data.scan.ScanState
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val libraryRepository: LibraryRepository,
    private val metadataEnricher: MetadataEnricher
) : ViewModel() {

    val apiKey: StateFlow<String> = settingsRepository.apiKey
    val syncOnlyWifi: StateFlow<Boolean> = settingsRepository.syncOnlyWifi
    val autoEnrich: StateFlow<Boolean> = settingsRepository.autoEnrich
    val scanState: StateFlow<ScanState> = libraryRepository.scanState
    val enrichState: StateFlow<EnrichState> = metadataEnricher.enrichState

    fun saveApiKey(key: String) {
        settingsRepository.setApiKey(key)
        if (key.isNotBlank()) {
            metadataEnricher.enrichAllMissingAsync()
        }
    }

    fun triggerEnrichAll(force: Boolean = false) {
        metadataEnricher.enrichAllMissingAsync(force)
    }

    fun dismissEnrichState() {
        metadataEnricher.dismissState()
    }

    fun setSyncOnlyWifi(enabled: Boolean) {
        settingsRepository.setSyncOnlyWifi(enabled)
    }

    fun setAutoEnrich(enabled: Boolean) {
        settingsRepository.setAutoEnrich(enabled)
    }

    fun triggerRescan() {
        libraryRepository.requestScan()
    }
}
