package id.smartplayer.ui.dashboard

import android.content.Context
import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.smartplayer.data.db.entities.EpisodeRow
import id.smartplayer.data.db.entities.SeriesSummary
import id.smartplayer.data.repo.LibraryRepository
import id.smartplayer.data.scan.ScanState
import id.smartplayer.ui.common.MediaDeleteHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DashboardFilter {
    ALL,
    IN_PROGRESS,
    UNFINISHED,
    SERIES,
    MOVIES,
    FINISHED
}

data class DashboardUiState(
    val continueWatching: List<EpisodeRow> = emptyList(),
    val collections: List<SeriesSummary> = emptyList(),
    val selectedFilter: DashboardFilter = DashboardFilter.ALL,
    val searchQuery: String = "",
    val scanState: ScanState = ScanState.Idle,
    val pendingDeleteSeries: SeriesSummary? = null
)

class DashboardViewModel(
    private val repository: LibraryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(DashboardFilter.ALL)
    val selectedFilter: StateFlow<DashboardFilter> = _selectedFilter.asStateFlow()

    private val _pendingDeleteSeries = MutableStateFlow<SeriesSummary?>(null)
    private var pendingDeleteMediaStoreIds: List<Long> = emptyList()

    private val contentFlow = combine(
        repository.observeDashboard(),
        repository.observeContinueWatching(),
        _selectedFilter,
        _searchQuery
    ) { allSeries, continueList, filter, query ->
        val filtered = allSeries.filter { series ->
            val matchesQuery = query.isBlank() || series.displayTitle.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                DashboardFilter.ALL -> true
                DashboardFilter.IN_PROGRESS -> series.finishedCount in 1 until series.episodeCount
                DashboardFilter.UNFINISHED -> series.finishedCount < series.episodeCount
                DashboardFilter.SERIES -> !series.isMovie
                DashboardFilter.MOVIES -> series.isMovie
                DashboardFilter.FINISHED -> series.isFinished
            }
            matchesQuery && matchesFilter
        }
        Triple(continueList, filtered, filter to query)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        contentFlow,
        repository.scanState,
        _pendingDeleteSeries
    ) { (continueList, filtered, filterAndQuery), scan, pendingDelete ->
        val (filter, query) = filterAndQuery
        DashboardUiState(
            continueWatching = continueList,
            collections = filtered,
            selectedFilter = filter,
            searchQuery = query,
            scanState = scan,
            pendingDeleteSeries = pendingDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun dismissScanBanner() {
        repository.dismissScanState()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: DashboardFilter) {
        _selectedFilter.value = filter
    }

    fun requestRescan() {
        repository.requestScan()
    }

    fun requestDeleteSeries(series: SeriesSummary) {
        _pendingDeleteSeries.value = series
    }

    fun dismissDeleteDialog() {
        _pendingDeleteSeries.value = null
        pendingDeleteMediaStoreIds = emptyList()
    }

    fun executeDeleteSeries(
        context: Context,
        onLaunchSender: (IntentSenderRequest) -> Unit
    ) {
        val series = _pendingDeleteSeries.value ?: return
        viewModelScope.launch {
            val episodes = repository.getEpisodes(series.id)
            if (episodes.isEmpty()) {
                repository.deleteSeries(series.id)
                dismissDeleteDialog()
                return@launch
            }
            val uris = episodes.map { Uri.parse(it.contentUri) }
            val mediaStoreIds = episodes.map { it.mediaStoreId }
            pendingDeleteMediaStoreIds = mediaStoreIds

            val intentSenderRequest = MediaDeleteHelper.buildDeleteIntentSender(context, uris)
            if (intentSenderRequest != null) {
                onLaunchSender(intentSenderRequest)
            } else {
                val success = MediaDeleteHelper.deleteDirectly(context, uris)
                if (success) {
                    onDeleteConfirmed()
                } else {
                    dismissDeleteDialog()
                }
            }
        }
    }

    fun onDeleteConfirmed() {
        val ids = pendingDeleteMediaStoreIds
        viewModelScope.launch {
            repository.deleteEpisodesByMediaStoreIds(ids)
            dismissDeleteDialog()
        }
    }
}
