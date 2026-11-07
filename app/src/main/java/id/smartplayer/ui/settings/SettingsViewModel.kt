package id.smartplayer.ui.settings

import androidx.lifecycle.ViewModel
import id.smartplayer.data.repo.LibraryRepository
import id.smartplayer.data.repo.SettingsRepository
import id.smartplayer.data.scan.ScanState
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    val apiKey: StateFlow<String> = settingsRepository.apiKey
    val syncOnlyWifi: StateFlow<Boolean> = settingsRepository.syncOnlyWifi
    val autoEnrich: StateFlow<Boolean> = settingsRepository.autoEnrich
    val scanState: StateFlow<ScanState> = libraryRepository.scanState

    fun saveApiKey(key: String) {
        settingsRepository.setApiKey(key)
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
