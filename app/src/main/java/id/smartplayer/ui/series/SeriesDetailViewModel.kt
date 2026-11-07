package id.smartplayer.ui.series

import android.content.Context
import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.smartplayer.data.db.entities.EpisodeRow
import id.smartplayer.data.db.entities.SeriesEntity
import id.smartplayer.data.repo.LibraryRepository
import id.smartplayer.ui.common.MediaDeleteHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DeleteTarget {
    data class Single(val episode: EpisodeRow) : DeleteTarget
    data class Bulk(val episodes: List<EpisodeRow>) : DeleteTarget
    data class Series(val series: SeriesEntity, val episodes: List<EpisodeRow>) : DeleteTarget
}

data class SeriesDetailUiState(
    val series: SeriesEntity? = null,
    val episodes: List<EpisodeRow> = emptyList(),
    val resumeEpisode: EpisodeRow? = null,
    val isLoading: Boolean = true,
    val isSelectionMode: Boolean = false,
    val selectedEpisodeIds: Set<Long> = emptySet(),
    val pendingDeleteTarget: DeleteTarget? = null
)

class SeriesDetailViewModel(
    private val seriesId: Long,
    private val repository: LibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesDetailUiState())
    val uiState: StateFlow<SeriesDetailUiState> = _uiState.asStateFlow()

    private var pendingDeleteMediaStoreIds: List<Long> = emptyList()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val series = repository.getSeriesById(seriesId)
            repository.observeEpisodes(seriesId).collect { episodeList ->
                val inProgress = episodeList.firstOrNull { !it.isFinished && (it.resumePositionMs ?: 0L) > 5000L }
                val firstUnfinished = episodeList.firstOrNull { !it.isFinished }
                val target = inProgress ?: firstUnfinished ?: episodeList.firstOrNull()

                val currentSelected = _uiState.value.selectedEpisodeIds
                val validSelected = currentSelected.intersect(episodeList.map { it.id }.toSet())

                _uiState.value = _uiState.value.copy(
                    series = series,
                    episodes = episodeList,
                    resumeEpisode = target,
                    isLoading = false,
                    selectedEpisodeIds = validSelected,
                    isSelectionMode = if (episodeList.isEmpty()) false else _uiState.value.isSelectionMode
                )
            }
        }
    }

    fun startSelectionMode(initialSelectedId: Long? = null) {
        val initialSet = if (initialSelectedId != null) setOf(initialSelectedId) else emptySet()
        _uiState.value = _uiState.value.copy(
            isSelectionMode = true,
            selectedEpisodeIds = initialSet
        )
    }

    fun exitSelectionMode() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = false,
            selectedEpisodeIds = emptySet()
        )
    }

    fun toggleEpisodeSelection(episodeId: Long) {
        val current = _uiState.value.selectedEpisodeIds
        val newSet = if (current.contains(episodeId)) {
            current - episodeId
        } else {
            current + episodeId
        }
        _uiState.value = _uiState.value.copy(selectedEpisodeIds = newSet)
    }

    fun selectAllEpisodes() {
        val allIds = _uiState.value.episodes.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedEpisodeIds = allIds)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedEpisodeIds = emptySet())
    }

    fun requestDeleteSingle(episode: EpisodeRow) {
        _uiState.value = _uiState.value.copy(pendingDeleteTarget = DeleteTarget.Single(episode))
    }

    fun requestDeleteSelected() {
        val selectedEpisodes = _uiState.value.episodes.filter { it.id in _uiState.value.selectedEpisodeIds }
        if (selectedEpisodes.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(pendingDeleteTarget = DeleteTarget.Bulk(selectedEpisodes))
        }
    }

    fun requestDeleteEntireSeries() {
        val series = _uiState.value.series ?: return
        val episodes = _uiState.value.episodes
        _uiState.value = _uiState.value.copy(pendingDeleteTarget = DeleteTarget.Series(series, episodes))
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(pendingDeleteTarget = null)
        pendingDeleteMediaStoreIds = emptyList()
    }

    fun executeDelete(
        context: Context,
        onLaunchSender: (IntentSenderRequest) -> Unit
    ) {
        val target = _uiState.value.pendingDeleteTarget ?: return
        val episodes = when (target) {
            is DeleteTarget.Single -> listOf(target.episode)
            is DeleteTarget.Bulk -> target.episodes
            is DeleteTarget.Series -> target.episodes
        }

        if (episodes.isEmpty()) {
            // If series has no episodes left in storage, just delete series from Room
            if (target is DeleteTarget.Series) {
                viewModelScope.launch {
                    repository.deleteSeries(target.series.id)
                }
            }
            dismissDeleteDialog()
            return
        }

        val uris = episodes.map { Uri.parse(it.contentUri) }
        val mediaStoreIds = episodes.map { it.mediaStoreId }
        pendingDeleteMediaStoreIds = mediaStoreIds

        val intentSenderRequest = MediaDeleteHelper.buildDeleteIntentSender(context, uris)
        if (intentSenderRequest != null) {
            onLaunchSender(intentSenderRequest)
        } else {
            // Fallback for API < 30
            val success = MediaDeleteHelper.deleteDirectly(context, uris)
            if (success) {
                onDeleteConfirmed()
            } else {
                dismissDeleteDialog()
            }
        }
    }

    fun onDeleteConfirmed() {
        val idsToDelete = pendingDeleteMediaStoreIds
        viewModelScope.launch {
            repository.deleteEpisodesByMediaStoreIds(idsToDelete)
            exitSelectionMode()
            dismissDeleteDialog()
        }
    }
}
