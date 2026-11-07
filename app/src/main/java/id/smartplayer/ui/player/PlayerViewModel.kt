package id.smartplayer.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.smartplayer.data.db.entities.EpisodeRow
import id.smartplayer.data.repo.LibraryRepository
import id.smartplayer.data.repo.PlaybackRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ResizeMode {
    FIT,
    ZOOM,
    STRETCH
}

data class PlayerUiState(
    val playlist: List<EpisodeRow> = emptyList(),
    val currentIndex: Int = 0,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val isLocked: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val resizeMode: ResizeMode = ResizeMode.FIT,
    val showControls: Boolean = true,
    val isEpisodeDrawerOpen: Boolean = false,
    val seekOverlayText: String? = null
) {
    val currentEpisode: EpisodeRow?
        get() = playlist.getOrNull(currentIndex)
}

class PlayerViewModel(
    private val seriesId: Long,
    private val initialEpisodeId: Long,
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        loadPlaylist()
    }

    private fun loadPlaylist() {
        viewModelScope.launch {
            val episodes = libraryRepository.getEpisodes(seriesId)
            val index = episodes.indexOfFirst { it.id == initialEpisodeId }.coerceAtLeast(0)
            _uiState.value = _uiState.value.copy(
                playlist = episodes,
                currentIndex = index
            )
        }
    }

    fun onPlayStateChanged(isPlaying: Boolean) {
        _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        if (isPlaying) {
            startTicker()
        } else {
            tickerJob?.cancel()
            persistCurrentProgress()
        }
    }

    fun onPositionChanged(positionMs: Long, durationMs: Long) {
        _uiState.value = _uiState.value.copy(
            currentPositionMs = positionMs,
            durationMs = durationMs
        )
    }

    fun onEpisodeTransition(newIndex: Int) {
        persistCurrentProgress()
        _uiState.value = _uiState.value.copy(currentIndex = newIndex)
    }

    fun seekRelative(deltaMs: Long) {
        val current = _uiState.value.currentPositionMs
        val dur = _uiState.value.durationMs
        val newPos = (current + deltaMs).coerceIn(0L, dur)
        val text = if (deltaMs > 0) "+${deltaMs / 1000}s" else "${deltaMs / 1000}s"
        _uiState.value = _uiState.value.copy(
            currentPositionMs = newPos,
            seekOverlayText = text
        )

        // Clear text after brief animation
        viewModelScope.launch {
            delay(700)
            if (_uiState.value.seekOverlayText == text) {
                _uiState.value = _uiState.value.copy(seekOverlayText = null)
            }
        }
    }

    fun toggleControls() {
        if (!_uiState.value.isLocked) {
            _uiState.value = _uiState.value.copy(showControls = !_uiState.value.showControls)
        }
    }

    fun toggleLock() {
        val newLock = !_uiState.value.isLocked
        _uiState.value = _uiState.value.copy(
            isLocked = newLock,
            showControls = !newLock
        )
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun toggleResizeMode() {
        val nextMode = when (_uiState.value.resizeMode) {
            ResizeMode.FIT -> ResizeMode.ZOOM
            ResizeMode.ZOOM -> ResizeMode.STRETCH
            ResizeMode.STRETCH -> ResizeMode.FIT
        }
        _uiState.value = _uiState.value.copy(resizeMode = nextMode)
    }

    fun toggleEpisodeDrawer(open: Boolean? = null) {
        val target = open ?: !_uiState.value.isEpisodeDrawerOpen
        _uiState.value = _uiState.value.copy(isEpisodeDrawerOpen = target)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(5000) // 5s ticker per PRD §4.4
                persistCurrentProgress()
            }
        }
    }

    fun persistCurrentProgress() {
        val current = _uiState.value.currentEpisode ?: return
        val pos = _uiState.value.currentPositionMs
        val dur = _uiState.value.durationMs
        viewModelScope.launch {
            playbackRepository.saveProgress(current.fingerprint, pos, dur)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        persistCurrentProgress()
    }
}
