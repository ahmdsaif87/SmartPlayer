package id.smartplayer.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Rational
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
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import id.smartplayer.SmartPlayerApp
import id.smartplayer.data.db.entities.EpisodeRow
import id.smartplayer.ui.theme.CinemaBackground
import id.smartplayer.ui.theme.CinemaPrimary
import id.smartplayer.ui.theme.CinemaProgressBg
import id.smartplayer.ui.theme.CinemaSecondary
import id.smartplayer.ui.theme.CinemaSurface
import id.smartplayer.ui.theme.CinemaSurfaceVariant
import id.smartplayer.ui.theme.CinemaTextPrimary
import id.smartplayer.ui.theme.CinemaTextSecondary
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

sealed interface GestureHudState {
    data class Brightness(val percent: Int) : GestureHudState
    data class Volume(val percent: Int, val isMuted: Boolean) : GestureHudState
}

class PlayerActivity : ComponentActivity() {

    private var exoPlayer: ExoPlayer? = null
    private lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Smooth window transition on entry
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

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
            PlayerScreen(
                viewModel = viewModel,
                onClose = { finish() },
                onEnterPip = { enterPipMode() }
            )
        }
    }

    fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (exoPlayer?.isPlaying == true) {
            enterPipMode()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        viewModel.setPipMode(isInPictureInPictureMode)
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.persistCurrentProgress()
        if (!isInPictureInPictureMode) {
            exoPlayer?.pause()
        }
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
    onEnterPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxAudioVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }

    var gestureHud by remember { mutableStateOf<GestureHudState?>(null) }
    var hudDismissEpoch by remember { mutableLongStateOf(0L) }

    // Auto-dismiss gesture HUD after 1200ms
    LaunchedEffect(gestureHud, hudDismissEpoch) {
        if (gestureHud != null) {
            delay(1200)
            if (SystemClock.elapsedRealtime() >= hudDismissEpoch) {
                gestureHud = null
            }
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekParameters(SeekParameters.EXACT)
            .build().apply {
                playWhenReady = true
            }
    }

    // Fast keyframe seek during tracker dragging
    var lastScrubSeekTime by remember { mutableLongStateOf(0L) }
    val onScrubSeek: (Long) -> Unit = remember(exoPlayer) {
        { targetMs ->
            val now = SystemClock.elapsedRealtime()
            if (now - lastScrubSeekTime >= 70L) {
                lastScrubSeekTime = now
                exoPlayer.setSeekParameters(SeekParameters.CLOSEST_SYNC)
                exoPlayer.seekTo(targetMs)
            }
        }
    }

    val onScrubFinished: (Long) -> Unit = remember(exoPlayer) {
        { finalMs ->
            exoPlayer.setSeekParameters(SeekParameters.EXACT)
            exoPlayer.seekTo(finalMs)
            viewModel.onPositionChanged(finalMs, exoPlayer.duration.coerceAtLeast(0L))
        }
    }

    // Helper functions for Track Extraction
    fun refreshTracks() {
        val tracks = exoPlayer.currentTracks
        val isTextDisabled = exoPlayer.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)

        // Audio
        val audios = mutableListOf<PlayerTrackInfo>()
        for (gIdx in 0 until tracks.groups.size) {
            val group = tracks.groups[gIdx]
            if (group.type == C.TRACK_TYPE_AUDIO) {
                for (tIdx in 0 until group.length) {
                    val format = group.getTrackFormat(tIdx)
                    val lang = format.language
                    val label = format.label ?: when {
                        lang?.startsWith("ja", ignoreCase = true) == true -> "Jepang"
                        lang?.startsWith("en", ignoreCase = true) == true -> "Inggris"
                        lang?.startsWith("id", ignoreCase = true) == true || lang?.startsWith("in", ignoreCase = true) == true -> "Indonesia"
                        else -> lang ?: "Audio Track ${audios.size + 1}"
                    }
                    audios.add(
                        PlayerTrackInfo(
                            groupIndex = gIdx,
                            trackIndex = tIdx,
                            label = label,
                            language = lang,
                            isSelected = group.isTrackSelected(tIdx)
                        )
                    )
                }
            }
        }

        // Subtitles
        val subs = mutableListOf<PlayerTrackInfo>()
        subs.add(
            PlayerTrackInfo(
                groupIndex = -1,
                trackIndex = -1,
                label = "Nonaktifkan Subtitle (Mati)",
                language = null,
                isSelected = isTextDisabled
            )
        )
        for (gIdx in 0 until tracks.groups.size) {
            val group = tracks.groups[gIdx]
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (tIdx in 0 until group.length) {
                    val format = group.getTrackFormat(tIdx)
                    val lang = format.language
                    val label = format.label ?: when {
                        lang?.startsWith("id", ignoreCase = true) == true || lang?.startsWith("in", ignoreCase = true) == true -> "Indonesia"
                        lang?.startsWith("en", ignoreCase = true) == true -> "Inggris"
                        lang?.startsWith("ja", ignoreCase = true) == true -> "Jepang"
                        else -> lang ?: "Subtitle ${subs.size}"
                    }
                    subs.add(
                        PlayerTrackInfo(
                            groupIndex = gIdx,
                            trackIndex = tIdx,
                            label = label,
                            language = lang,
                            isSelected = !isTextDisabled && group.isTrackSelected(tIdx)
                        )
                    )
                }
            }
        }

        viewModel.updateAvailableTracks(audios, subs)
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

    // Auto-advance when countdown reaches 0
    LaunchedEffect(uiState.nextEpisodeCountdown) {
        if (uiState.nextEpisodeCountdown == 0) {
            if (exoPlayer.hasNextMediaItem()) {
                exoPlayer.seekToNextMediaItem()
            }
            viewModel.dismissCountdown()
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

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    if (exoPlayer.hasNextMediaItem() && !uiState.isCountdownDismissed) {
                        viewModel.startNextCountdown(initialSeconds = 3)
                    }
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                refreshTracks()
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Touch gesture state holders
    var isLeftSwipe by remember { mutableStateOf(false) }
    var currentBrightnessRatio by remember { mutableFloatStateOf(0.5f) }
    var currentVolumeRatio by remember { mutableFloatStateOf(0.5f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            // Tap and double tap gestures
            .pointerInput(uiState.isLocked, uiState.isInPipMode) {
                if (uiState.isInPipMode) return@pointerInput
                detectTapGestures(
                    onTap = { viewModel.toggleControls() },
                    onDoubleTap = { offset ->
                        if (!uiState.isLocked) {
                            val screenWidth = size.width
                            if (offset.x < screenWidth / 2) {
                                // Double tap left: seek -10s
                                val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                exoPlayer.setSeekParameters(SeekParameters.DEFAULT)
                                exoPlayer.seekTo(newPos)
                                viewModel.seekRelative(-10000L)
                            } else {
                                // Double tap right: seek +10s
                                val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(exoPlayer.duration)
                                exoPlayer.setSeekParameters(SeekParameters.DEFAULT)
                                exoPlayer.seekTo(newPos)
                                viewModel.seekRelative(10000L)
                            }
                        }
                    }
                )
            }
            // Vertical Drag Gesture for Brightness (Left) & Volume (Right)
            .pointerInput(uiState.isLocked, uiState.isInPipMode) {
                if (uiState.isLocked || uiState.isInPipMode) return@pointerInput
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isLeftSwipe = offset.x < size.width / 2f
                        if (isLeftSwipe) {
                            val lp = activity?.window?.attributes
                            val cur = lp?.screenBrightness ?: -1f
                            currentBrightnessRatio = if (cur >= 0f) cur else {
                                try {
                                    Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
                                } catch (e: Exception) {
                                    0.5f
                                }
                            }
                        } else {
                            val vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            currentVolumeRatio = vol.toFloat() / maxAudioVolume
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        // Drag up is negative dragAmount -> increase
                        val deltaRatio = -dragAmount / size.height * 1.5f
                        if (isLeftSwipe) {
                            currentBrightnessRatio = (currentBrightnessRatio + deltaRatio).coerceIn(0.01f, 1f)
                            val lp = activity?.window?.attributes
                            if (lp != null) {
                                lp.screenBrightness = currentBrightnessRatio
                                activity.window.attributes = lp
                            }
                            val pct = (currentBrightnessRatio * 100).roundToInt()
                            gestureHud = GestureHudState.Brightness(pct)
                            hudDismissEpoch = SystemClock.elapsedRealtime() + 1200L
                        } else {
                            currentVolumeRatio = (currentVolumeRatio + deltaRatio).coerceIn(0f, 1f)
                            val targetVol = (currentVolumeRatio * maxAudioVolume).roundToInt()
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                            val pct = (currentVolumeRatio * 100).roundToInt()
                            gestureHud = GestureHudState.Volume(pct, isMuted = targetVol == 0)
                            hudDismissEpoch = SystemClock.elapsedRealtime() + 1200L
                        }
                    },
                    onDragEnd = {
                        hudDismissEpoch = SystemClock.elapsedRealtime() + 1200L
                    },
                    onDragCancel = {
                        gestureHud = null
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
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    keepScreenOn = true
                    resizeMode = when (uiState.resizeMode) {
                        ResizeMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        ResizeMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        ResizeMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                    subtitleView?.apply {
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
                }
            },
            update = { view ->
                view.resizeMode = when (uiState.resizeMode) {
                    ResizeMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    ResizeMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    ResizeMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Seek HUD Bubble Animation (+10s / -10s)
        if (uiState.seekOverlayText != null && !uiState.isInPipMode) {
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

        // Gesture HUD Overlay (Brightness on Left, Volume on Right)
        if (gestureHud != null && !uiState.isInPipMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp),
                contentAlignment = if (gestureHud is GestureHudState.Brightness) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC15151E)),
                    modifier = Modifier
                        .width(130.dp)
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (val hud = gestureHud!!) {
                            is GestureHudState.Brightness -> {
                                Icon(
                                    imageVector = Icons.Default.BrightnessMedium,
                                    contentDescription = "Kecerahan",
                                    tint = CinemaPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "${hud.percent}%",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                LinearProgressIndicator(
                                    progress = { hud.percent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp),
                                    color = CinemaPrimary,
                                    trackColor = CinemaProgressBg
                                )
                            }
                            is GestureHudState.Volume -> {
                                Icon(
                                    imageVector = if (hud.isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Volume",
                                    tint = CinemaPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "${hud.percent}%",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                LinearProgressIndicator(
                                    progress = { hud.percent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp),
                                    color = CinemaPrimary,
                                    trackColor = CinemaProgressBg
                                )
                            }
                        }
                    }
                }
            }
        }

        // Auto-Play Next Episode Floating Card (ala Netflix)
        if (uiState.nextEpisodeCountdown != null && uiState.nextEpisode != null && !uiState.isInPipMode) {
            val nextEp = uiState.nextEpisode!!
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEE15151E)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 80.dp)
                    .width(320.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Episode Berikutnya",
                        color = CinemaTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (nextEp.episodeNumber != null) "Episode ${nextEp.episodeNumber} — ${nextEp.displayName}" else nextEp.displayName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Putar otomatis dalam ${uiState.nextEpisodeCountdown} detik",
                        color = CinemaPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { viewModel.dismissCountdown() }) {
                            Text("Batal", color = CinemaTextSecondary, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (exoPlayer.hasNextMediaItem()) {
                                    exoPlayer.seekToNextMediaItem()
                                }
                                viewModel.dismissCountdown()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaPrimary),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Putar Sekarang", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = uiState.showControls && !uiState.isLocked && !uiState.isInPipMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerOverlayControls(
                uiState = uiState,
                onClose = onClose,
                onEnterPip = onEnterPip,
                onPlayPauseToggle = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onScrubSeek = onScrubSeek,
                onScrubFinished = onScrubFinished,
                onPrevious = { if (exoPlayer.hasPreviousMediaItem()) exoPlayer.seekToPreviousMediaItem() },
                onNext = { if (exoPlayer.hasNextMediaItem()) exoPlayer.seekToNextMediaItem() },
                onToggleLock = { viewModel.toggleLock() },
                onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                onToggleResize = { viewModel.toggleResizeMode() },
                onToggleEpisodes = { viewModel.toggleEpisodeDrawer() },
                onOpenAudioSelector = { viewModel.toggleAudioSelector(true) },
                onOpenSubtitleSelector = { viewModel.toggleSubtitleSelector(true) }
            )
        }

        // Locked HUD Button
        if (uiState.isLocked && !uiState.isInPipMode) {
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
            visible = uiState.isEpisodeDrawerOpen && !uiState.isInPipMode,
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

        // Audio Track Selector Dialog
        if (uiState.isAudioSelectorOpen && !uiState.isInPipMode) {
            TrackSelectionDialog(
                title = "Pilih Trek Audio",
                tracks = uiState.audioTracks,
                onSelect = { track ->
                    val group = exoPlayer.currentTracks.groups.getOrNull(track.groupIndex)
                    if (group != null) {
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                            .buildUpon()
                            .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, track.trackIndex))
                            .build()
                        refreshTracks()
                    }
                    viewModel.toggleAudioSelector(false)
                },
                onDismiss = { viewModel.toggleAudioSelector(false) }
            )
        }

        // Subtitle Track Selector Dialog
        if (uiState.isSubtitleSelectorOpen && !uiState.isInPipMode) {
            TrackSelectionDialog(
                title = "Pilih Trek Subtitle",
                tracks = uiState.subtitleTracks,
                onSelect = { track ->
                    if (track.groupIndex == -1) {
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                            .build()
                    } else {
                        val group = exoPlayer.currentTracks.groups.getOrNull(track.groupIndex)
                        if (group != null) {
                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                .buildUpon()
                                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, track.trackIndex))
                                .build()
                        }
                    }
                    refreshTracks()
                    viewModel.toggleSubtitleSelector(false)
                },
                onDismiss = { viewModel.toggleSubtitleSelector(false) }
            )
        }
    }
}

@Composable
private fun TrackSelectionDialog(
    title: String,
    tracks: List<PlayerTrackInfo>,
    onSelect: (PlayerTrackInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (tracks.isEmpty()) {
                Text(
                    text = "Tidak ada trek tambahan yang tersedia.",
                    color = CinemaTextSecondary,
                    fontSize = 14.sp
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(tracks) { _, track ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(track) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (track.isSelected) CinemaPrimary.copy(alpha = 0.25f) else CinemaSurfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (track.isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (track.isSelected) CinemaPrimary else CinemaTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = track.label,
                                        color = if (track.isSelected) Color.White else CinemaTextPrimary,
                                        fontWeight = if (track.isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                    if (track.language != null && track.language != track.label) {
                                        Text(
                                            text = track.language.uppercase(),
                                            color = CinemaTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = CinemaSecondary)
            }
        },
        containerColor = CinemaSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun PlayerOverlayControls(
    uiState: PlayerUiState,
    onClose: () -> Unit,
    onEnterPip: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onScrubSeek: (Long) -> Unit,
    onScrubFinished: (Long) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleLock: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onToggleResize: () -> Unit,
    onToggleEpisodes: () -> Unit,
    onOpenAudioSelector: () -> Unit,
    onOpenSubtitleSelector: () -> Unit
) {
    val current = uiState.currentEpisode
    val pos = uiState.currentPositionMs
    val dur = uiState.durationMs

    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val currentProgress = if (isDragging) dragFraction else if (dur > 0) (pos.toFloat() / dur).coerceIn(0f, 1f) else 0f
    val displayedPos = if (isDragging) (dragFraction * dur).toLong().coerceIn(0L, dur) else pos

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp)
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
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
            }

            // Top action buttons: PiP, Audio, Subtitle, Aspect Ratio
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // PiP Button
                IconButton(onClick = onEnterPip) {
                    Icon(
                        imageVector = Icons.Default.PictureInPictureAlt,
                        contentDescription = "Picture in Picture",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Audio Track selector
                IconButton(onClick = onOpenAudioSelector) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = "Pilih Audio",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Subtitle Track selector
                IconButton(onClick = onOpenSubtitleSelector) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = "Pilih Subtitle",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Aspect ratio toggle
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
            // Scrub tooltip bubble while dragging
            if (isDragging) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xDD15151E), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${formatMs(displayedPos)} / ${formatMs(dur)}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Timeline Time labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(displayedPos),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "-${formatMs((dur - displayedPos).coerceAtLeast(0L))}",
                    color = CinemaTextSecondary,
                    fontSize = 12.sp
                )
            }

            // Slider Scrub Bar
            Slider(
                value = currentProgress,
                onValueChange = { frac ->
                    isDragging = true
                    dragFraction = frac
                    val targetMs = (frac * dur).toLong()
                    onScrubSeek(targetMs)
                },
                onValueChangeFinished = {
                    val finalMs = (dragFraction * dur).toLong()
                    onScrubFinished(finalMs)
                    isDragging = false
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
