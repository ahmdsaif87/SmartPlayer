package id.smartplayer.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.smartplayer.data.db.entities.EpisodeRow
import id.smartplayer.data.db.entities.SeriesEntity
import id.smartplayer.data.repo.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SeriesDetailUiState(
    val series: SeriesEntity? = null,
    val episodes: List<EpisodeRow> = emptyList(),
    val resumeEpisode: EpisodeRow? = null,
    val isLoading: Boolean = true
)

class SeriesDetailViewModel(
    private val seriesId: Long,
    private val repository: LibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesDetailUiState())
    val uiState: StateFlow<SeriesDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val series = repository.getSeriesById(seriesId)
            repository.observeEpisodes(seriesId).collect { episodeList ->
                // Calculate resume target:
                // 1. Unfinished episode with existing progress
                // 2. First unfinished episode
                // 3. First episode (if all finished)
                val inProgress = episodeList.firstOrNull { !it.isFinished && (it.resumePositionMs ?: 0L) > 5000L }
                val firstUnfinished = episodeList.firstOrNull { !it.isFinished }
                val target = inProgress ?: firstUnfinished ?: episodeList.firstOrNull()

                _uiState.value = SeriesDetailUiState(
                    series = series,
                    episodes = episodeList,
                    resumeEpisode = target,
                    isLoading = false
                )
            }
        }
    }
}
