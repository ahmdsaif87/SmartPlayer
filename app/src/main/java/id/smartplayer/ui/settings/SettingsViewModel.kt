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

    val scanState: StateFlow<ScanState> = libraryRepository.scanState
    val excludeCamera: StateFlow<Boolean> = settingsRepository.excludeCamera

    fun setExcludeCamera(enabled: Boolean) {
        settingsRepository.setExcludeCamera(enabled)
        triggerRescan()
    }

    fun triggerRescan() {
        libraryRepository.requestScan()
    }

    fun dismissScanState() {
        libraryRepository.dismissScanState()
    }
}
