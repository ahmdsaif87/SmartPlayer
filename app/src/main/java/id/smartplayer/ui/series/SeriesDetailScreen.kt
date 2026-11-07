package id.smartplayer.ui.series

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeriesDetailScreen(
    viewModel: SeriesDetailViewModel,
    onBackClick: () -> Unit,
    onPlayClick: (Long, Long) -> Unit, // episodeId, seriesId
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Activity launcher for Android Scoped Storage media deletion
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onDeleteConfirmed()
        } else {
            viewModel.dismissDeleteDialog()
        }
    }

    // Auto-navigate back if the series is deleted or has no episodes remaining
    LaunchedEffect(uiState.isLoading, uiState.episodes.isEmpty(), uiState.series == null) {
        if (!uiState.isLoading && (uiState.series == null || uiState.episodes.isEmpty())) {
            onBackClick()
        }
    }

    if (uiState.isLoading || uiState.series == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(CinemaBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CinemaPrimary)
        }
        return
    }

    val series = uiState.series!!
    val episodes = uiState.episodes
    val resumeTarget = uiState.resumeEpisode

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBackground)
    ) {
        // --- Selection Mode Top Bar (Active when selecting multiple episodes) ---
        AnimatedVisibility(
            visible = uiState.isSelectionMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CinemaSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.exitSelectionMode() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Batal Pilih",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${uiState.selectedEpisodeIds.size} dipilih",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val allSelected = episodes.isNotEmpty() && uiState.selectedEpisodeIds.size == episodes.size
                    TextButton(
                        onClick = {
                            if (allSelected) viewModel.clearSelection() else viewModel.selectAllEpisodes()
                        }
                    ) {
                        Text(
                            text = if (allSelected) "Batal Semua" else "Pilih Semua",
                            color = CinemaSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { viewModel.requestDeleteSelected() },
                        enabled = uiState.selectedEpisodeIds.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus episode terpilih",
                            tint = if (uiState.selectedEpisodeIds.isNotEmpty()) CinemaPrimary else CinemaTextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // --- Hero Header with Gradient Scrim ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    VideoThumbnail(
                        mediaStoreId = series.coverMediaStoreId,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dark gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0x990A0A0C), CinemaBackground),
                                    startY = 50f
                                )
                            )
                    )

                    // Top Action Bar on Hero
                    if (!uiState.isSelectionMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .align(Alignment.TopStart),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back button
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0x66000000), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Right Action buttons: Enter Selection Mode & Delete Series
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { viewModel.startSelectionMode() },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0x66000000), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Checklist,
                                        contentDescription = "Pilih Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.requestDeleteEntireSeries() },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0x66000000), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Hapus Seri",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Metadata & Overview ---
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = series.displayTitle,
                        color = CinemaTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Info row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val seasonLabel = if (series.isMovie) "Film Standalone" else "Season ${series.seasonNumber}"
                        Text(text = seasonLabel, color = CinemaTextSecondary, fontSize = 13.sp)
                        Text(text = "•", color = CinemaTextSecondary)
                        val epsCountText = "${episodes.size} Episode"
                        Text(text = epsCountText, color = CinemaTextSecondary, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Netflix-Style Primary CTA Button (hidden in selection mode)
                    if (resumeTarget != null && !uiState.isSelectionMode) {
                        val ctaText = when {
                            resumeTarget.isFinished -> "Tonton Ulang (Eps 1)"
                            (resumeTarget.resumePositionMs ?: 0L) > 5000L -> "Lanjutkan Nonton (Eps ${resumeTarget.episodeNumber ?: 1})"
                            else -> "Mulai Nonton (Eps ${resumeTarget.episodeNumber ?: 1})"
                        }

                        Button(
                            onClick = { onPlayClick(resumeTarget.id, series.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ctaText,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAFTAR EPISODE",
                            color = CinemaTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        if (!uiState.isSelectionMode) {
                            TextButton(onClick = { viewModel.startSelectionMode() }) {
                                Text(
                                    text = "Pilih Video",
                                    color = CinemaSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // --- Flat Episode List ---
            items(episodes, key = { it.id }) { ep ->
                val isSelected = uiState.selectedEpisodeIds.contains(ep.id)
                EpisodeItemRow(
                    episode = ep,
                    isSelectionMode = uiState.isSelectionMode,
                    isSelected = isSelected,
                    onClick = {
                        if (uiState.isSelectionMode) {
                            viewModel.toggleEpisodeSelection(ep.id)
                        } else {
                            onPlayClick(ep.id, series.id)
                        }
                    },
                    onLongClick = {
                        if (!uiState.isSelectionMode) {
                            viewModel.startSelectionMode(initialSelectedId = ep.id)
                        }
                    },
                    onDeleteClick = {
                        viewModel.requestDeleteSingle(ep)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // --- Delete Confirmation Dialog ---
    if (uiState.pendingDeleteTarget != null) {
        val target = uiState.pendingDeleteTarget!!
        val title = when (target) {
            is DeleteTarget.Single -> "Hapus Video?"
            is DeleteTarget.Bulk -> "Hapus ${target.episodes.size} Video?"
            is DeleteTarget.Series -> "Hapus Seluruh Seri?"
        }
        val message = when (target) {
            is DeleteTarget.Single -> "Video \"${target.episode.displayName}\" akan dihapus secara permanen dari penyimpanan perangkat."
            is DeleteTarget.Bulk -> "${target.episodes.size} video terpilih akan dihapus secara permanen dari penyimpanan perangkat."
            is DeleteTarget.Series -> "Semua ${target.episodes.size} episode dari serial \"${target.series.displayTitle}\" akan dihapus secara permanen dari penyimpanan perangkat."
        }

        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = {
                Text(
                    text = title,
                    color = CinemaTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = message,
                    color = CinemaTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executeDelete(context) { request ->
                            deleteLauncher.launch(request)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaPrimary)
                ) {
                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Batal", color = CinemaTextSecondary)
                }
            },
            containerColor = CinemaSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpisodeItemRow(
    episode: EpisodeRow,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CinemaSurfaceVariant else CinemaSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox in selection mode
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = CinemaPrimary,
                        uncheckedColor = CinemaTextSecondary,
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.padding(end = 6.dp)
                )
            }

            // Episode Thumbnail (16:9)
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                VideoThumbnail(
                    posterUrl = null,
                    mediaStoreId = episode.mediaStoreId,
                    modifier = Modifier.fillMaxSize()
                )

                // Finished checkmark or progress bar
                if (episode.isFinished) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xCC22C55E), RoundedCornerShape(4.dp))
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selesai",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else if ((episode.resumePositionMs ?: 0L) > 5000L) {
                    val progress = if (episode.durationMs > 0) {
                        (episode.resumePositionMs ?: 0L).toFloat() / episode.durationMs
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = CinemaPrimary,
                        trackColor = CinemaProgressBg
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata text
            Column(modifier = Modifier.weight(1f)) {
                val title = if (episode.episodeNumber != null) {
                    "Episode ${episode.episodeNumber}"
                } else {
                    episode.displayName
                }

                Text(
                    text = title,
                    color = CinemaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Duration
                    val durationMin = episode.durationMs / 60000
                    Text(
                        text = "${durationMin} mnt",
                        color = CinemaTextSecondary,
                        fontSize = 11.sp
                    )

                    // File size
                    val sizeMb = episode.sizeBytes / (1024 * 1024)
                    Text(text = "•", color = CinemaTextSecondary)
                    Text(
                        text = "${sizeMb} MB",
                        color = CinemaTextSecondary,
                        fontSize = 11.sp
                    )

                    // Resolution chip
                    if (!episode.resolution.isNullOrBlank()) {
                        Text(text = "•", color = CinemaTextSecondary)
                        Box(
                            modifier = Modifier
                                .background(CinemaSurfaceVariant, RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = episode.resolution.uppercase(),
                                color = CinemaSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Single Delete Icon Button (hidden in selection mode)
            if (!isSelectionMode) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus episode",
                        tint = CinemaTextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
