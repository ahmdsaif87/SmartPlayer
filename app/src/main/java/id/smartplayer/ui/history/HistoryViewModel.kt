package id.smartplayer.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.smartplayer.data.db.entities.HistoryItemRow
import id.smartplayer.data.repo.LibraryRepository
import id.smartplayer.data.repo.PlaybackRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    val historyItems: StateFlow<List<HistoryItemRow>> = libraryRepository.observePlaybackHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteHistoryItem(episodeId: Long) {
        viewModelScope.launch {
            playbackRepository.deleteHistoryByEpisodeId(episodeId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            playbackRepository.clearHistory()
        }
    }
}
