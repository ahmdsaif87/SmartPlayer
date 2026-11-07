package id.smartplayer.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.smartplayer.data.ai.EnrichState
import id.smartplayer.data.ai.MetadataEnricher
import id.smartplayer.data.db.entities.EpisodeRow
import id.smartplayer.data.db.entities.SeriesSummary
import id.smartplayer.data.repo.LibraryRepository
import id.smartplayer.data.scan.ScanState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
    val enrichState: EnrichState = EnrichState.Idle
)

class DashboardViewModel(
    private val repository: LibraryRepository,
    private val metadataEnricher: MetadataEnricher
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(DashboardFilter.ALL)
    val selectedFilter: StateFlow<DashboardFilter> = _selectedFilter.asStateFlow()

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
        metadataEnricher.enrichState
    ) { (continueList, filtered, filterAndQuery), scan, enrich ->
        val (filter, query) = filterAndQuery
        DashboardUiState(
            continueWatching = continueList,
            collections = filtered,
            selectedFilter = filter,
            searchQuery = query,
            scanState = scan,
            enrichState = enrich
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun dismissEnrichState() {
        metadataEnricher.dismissState()
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
}
