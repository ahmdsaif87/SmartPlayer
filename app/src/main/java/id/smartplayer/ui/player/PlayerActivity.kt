package id.smartplayer.ui.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import id.smartplayer.SmartPlayerApp
import id.smartplayer.data.db.entities.EpisodeRow
import id.smartplayer.ui.common.VideoThumbnail
import id.smartplayer.ui.theme.CinemaBackground
import id.smartplayer.ui.theme.CinemaPrimary
import id.smartplayer.ui.theme.CinemaProgressBg
import id.smartplayer.ui.theme.CinemaSecondary
import id.smartplayer.ui.theme.CinemaSurface
import id.smartplayer.ui.theme.CinemaSurfaceVariant
import id.smartplayer.ui.theme.CinemaTextPrimary
import id.smartplayer.ui.theme.CinemaTextSecondary
import kotlinx.coroutines.delay

class PlayerActivity : ComponentActivity() {

    private var exoPlayer: ExoPlayer? = null
    private lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Immersive sticky mode
        hideSystemBars()

        val seriesId = intent.getLongExtra(EXTRA_SERIES_ID, -1L)
        val episodeId = intent.getLongExtra(EXTRA_EPISODE_ID, -1L)

        val app = application as SmartPlayerApp
        viewModel = PlayerViewModel(
            seriesId = seriesId,
            initialEpisodeId = episodeId,
            libraryRepository = app.container.libraryRepository,
            playbackRepository = app.container.playbackRepository
        )

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            PlayerScreen(
                viewModel = viewModel,
                onClose = { finish() }
            )
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onPause() {
        super.onPause()
        viewModel.persistCurrentProgress()
        exoPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.persistCurrentProgress()
        exoPlayer?.release()
        exoPlayer = null
    }

    companion object {
        private const val EXTRA_SERIES_ID = "extra_series_id"
        private const val EXTRA_EPISODE_ID = "extra_episode_id"

        fun createIntent(context: Context, seriesId: Long, episodeId: Long): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_SERIES_ID, seriesId)
                putExtra(EXTRA_EPISODE_ID, episodeId)
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Bind playlist
    LaunchedEffect(uiState.playlist) {
        if (uiState.playlist.isNotEmpty()) {
            val items = uiState.playlist.map { MediaItem.fromUri(Uri.parse(it.contentUri)) }
            exoPlayer.setMediaItems(items)

            val startIndex = uiState.currentIndex
            val targetEp = uiState.playlist.getOrNull(startIndex)
            val startPos = if (targetEp != null && (targetEp.resumePositionMs ?: 0L) > 5000L) {
                (targetEp.resumePositionMs!! - 2000L).coerceAtLeast(0L)
            } else 0L

            exoPlayer.seekTo(startIndex, startPos)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    // Speed sync
    LaunchedEffect(uiState.playbackSpeed) {
        exoPlayer.setPlaybackSpeed(uiState.playbackSpeed)
    }

    // Position & duration polling
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            if (exoPlayer.isPlaying) {
                viewModel.onPositionChanged(
                    positionMs = exoPlayer.currentPosition,
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                )
            }
        }
    }

    // Player listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                viewModel.onPlayStateChanged(isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                viewModel.onEpisodeTransition(exoPlayer.currentMediaItemIndex)
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(uiState.isLocked) {
                detectTapGestures(
                    onTap = { viewModel.toggleControls() },
                    onDoubleTap = { offset ->
                        if (!uiState.isLocked) {
                            val screenWidth = size.width
                            if (offset.x < screenWidth / 2) {
                                // Double tap left: seek -10s
                                val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                exoPlayer.seekTo(newPos)
                                viewModel.seekRelative(-10000L)
                            } else {
                                // Double tap right: seek +10s
                                val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(exoPlayer.duration)
                                exoPlayer.seekTo(newPos)
                                viewModel.seekRelative(10000L)
                            }
                        }
                    }
                )
            }
    ) {
        // Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = when (uiState.resizeMode) {
                        ResizeMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        ResizeMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        ResizeMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                    subtitleView?.apply {
                        setStyle(
                            CaptionStyleCompat(
                                /* foregroundColor = */ android.graphics.Color.WHITE,
                                /* backgroundColor = */ android.graphics.Color.TRANSPARENT,
                                /* windowColor = */ android.graphics.Color.TRANSPARENT,
                                /* edgeType = */ CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                                /* edgeColor = */ android.graphics.Color.argb(220, 0, 0, 0),
                                /* typeface = */ null
                            )
                        )
                        setApplyEmbeddedStyles(false)
                        setApplyEmbeddedFontSizes(false)
                        setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 0.92f)
                        setBottomPaddingFraction(0.06f)
                    }
                }
            },
            update = { view ->
                view.resizeMode = when (uiState.resizeMode) {
                    ResizeMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    ResizeMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    ResizeMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                }
                view.subtitleView?.apply {
                    setStyle(
                        CaptionStyleCompat(
                            android.graphics.Color.WHITE,
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                            CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                            android.graphics.Color.argb(220, 0, 0, 0),
                            null
                        )
                    )
                    setApplyEmbeddedStyles(false)
                    setApplyEmbeddedFontSizes(false)
                    setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 0.92f)
                    setBottomPaddingFraction(0.06f)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Seek HUD Bubble Animation
        if (uiState.seekOverlayText != null) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0x99000000), CircleShape)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.seekOverlayText!!,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = uiState.showControls && !uiState.isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerOverlayControls(
                uiState = uiState,
                onClose = onClose,
                onPlayPauseToggle = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onSeekTo = { newPos -> exoPlayer.seekTo(newPos) },
                onPrevious = { if (exoPlayer.hasPreviousMediaItem()) exoPlayer.seekToPreviousMediaItem() },
                onNext = { if (exoPlayer.hasNextMediaItem()) exoPlayer.seekToNextMediaItem() },
                onToggleLock = { viewModel.toggleLock() },
                onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                onToggleResize = { viewModel.toggleResizeMode() },
                onToggleEpisodes = { viewModel.toggleEpisodeDrawer() }
            )
        }

        // Locked HUD Button
        if (uiState.isLocked) {
            IconButton(
                onClick = { viewModel.toggleLock() },
                modifier = Modifier
                    .padding(24.dp)
                    .size(48.dp)
                    .background(Color(0x99000000), CircleShape)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Buka Kunci Layar",
                    tint = CinemaPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Netflix-Style Slide-over In-Player Episode Drawer
        AnimatedVisibility(
            visible = uiState.isEpisodeDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            EpisodeDrawer(
                playlist = uiState.playlist,
                currentIndex = uiState.currentIndex,
                onSelectEpisode = { index ->
                    exoPlayer.seekTo(index, 0L)
                    viewModel.toggleEpisodeDrawer(false)
                },
                onClose = { viewModel.toggleEpisodeDrawer(false) }
            )
        }
    }
}

@Composable
private fun PlayerOverlayControls(
    uiState: PlayerUiState,
    onClose: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleLock: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onToggleResize: () -> Unit,
    onToggleEpisodes: () -> Unit
) {
    val current = uiState.currentEpisode
    val pos = uiState.currentPositionMs
    val dur = uiState.durationMs

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                val title = if (current?.episodeNumber != null) {
                    "Episode ${current.episodeNumber} — ${current.displayName}"
                } else {
                    current?.displayName ?: "Video Player"
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleResize) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = "Mode Ukuran",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = when (uiState.resizeMode) {
                                ResizeMode.FIT -> "Fit"
                                ResizeMode.ZOOM -> "Zoom"
                                ResizeMode.STRETCH -> "Stretch"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- Center Play/Pause & Seek Buttons ---
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0x66000000), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Sebelumnya",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = onPlayPauseToggle,
                modifier = Modifier
                    .size(64.dp)
                    .background(CinemaPrimary, CircleShape)
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Jeda" else "Putar",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0x66000000), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Berikutnya",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // --- Bottom Controls & Scrub Bar ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.BottomCenter)
        ) {
            // Timeline Time labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(pos),
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = "-${formatMs((dur - pos).coerceAtLeast(0L))}",
                    color = CinemaTextSecondary,
                    fontSize = 12.sp
                )
            }

            // Slider Scrub Bar
            val sliderValue = if (dur > 0) pos.toFloat() / dur else 0f
            Slider(
                value = sliderValue,
                onValueChange = { frac ->
                    onSeekTo((frac * dur).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = CinemaPrimary,
                    activeTrackColor = CinemaPrimary,
                    inactiveTrackColor = CinemaProgressBg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Bottom action icons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onToggleLock) {
                    Icon(
                        imageVector = if (uiState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Kunci Layar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Speed selector toggle
                    val speedOptions = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    Text(
                        text = "${uiState.playbackSpeed}x",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                val currIdx = speedOptions.indexOf(uiState.playbackSpeed)
                                val nextIdx = (currIdx + 1) % speedOptions.size
                                onSpeedChange(speedOptions[nextIdx])
                            }
                            .background(Color(0x66000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    // Netflix In-Player Episode Drawer Trigger
                    IconButton(onClick = onToggleEpisodes) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Daftar Episode",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(text = "Episodes", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeDrawer(
    playlist: List<EpisodeRow>,
    currentIndex: Int,
    onSelectEpisode: (Int) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp),
        color = CinemaSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Daftar Episode",
                    color = CinemaTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = CinemaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(playlist, key = { _, ep -> ep.id }) { idx, ep ->
                    val isCurrent = idx == currentIndex
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectEpisode(idx) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) CinemaPrimary.copy(alpha = 0.2f) else CinemaSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (ep.episodeNumber != null) "Eps ${ep.episodeNumber}" else "Movie",
                                color = if (isCurrent) CinemaPrimary else CinemaTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.width(60.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ep.displayName,
                                color = CinemaTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
