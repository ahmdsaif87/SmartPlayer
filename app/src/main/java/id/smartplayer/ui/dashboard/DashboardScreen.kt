package id.smartplayer.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import id.smartplayer.data.db.entities.EpisodeRow
import id.smartplayer.data.db.entities.SeriesSummary
import id.smartplayer.data.scan.ScanState
import id.smartplayer.ui.common.VideoThumbnail
import id.smartplayer.ui.theme.CinemaBackground
import id.smartplayer.ui.theme.CinemaPrimary
import id.smartplayer.ui.theme.CinemaProgressBg
import id.smartplayer.ui.theme.CinemaSecondary
import id.smartplayer.ui.theme.CinemaSurface
import id.smartplayer.ui.theme.CinemaSurfaceVariant
import id.smartplayer.ui.theme.CinemaTextPrimary
import id.smartplayer.ui.theme.CinemaTextSecondary

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSeriesClick: (Long) -> Unit,
    onEpisodePlayClick: (Long, Long) -> Unit, // episodeId, seriesId
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBackground)
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (!isSearchExpanded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Smart",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Player",
                        color = CinemaPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isSearchExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.requestRescan() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Pindai Ulang",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Cari judul serial atau film...", color = CinemaTextSecondary, fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CinemaTextPrimary,
                        unfocusedTextColor = CinemaTextPrimary,
                        focusedContainerColor = CinemaSurface,
                        unfocusedContainerColor = CinemaSurface,
                        focusedBorderColor = CinemaPrimary,
                        unfocusedBorderColor = CinemaSurfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    trailingIcon = {
                        IconButton(onClick = {
                            viewModel.onSearchQueryChanged("")
                            isSearchExpanded = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup Pencarian",
                                tint = CinemaTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- Live Scan Status Feedback Banner ---
        var isScanBannerDismissed by remember(uiState.scanState) { mutableStateOf(false) }

        LaunchedEffect(uiState.scanState) {
            if (uiState.scanState is ScanState.Done) {
                delay(6000)
                isScanBannerDismissed = true
            }
        }

        if (!isScanBannerDismissed) {
            when (val scan = uiState.scanState) {
                is ScanState.Scanning -> {
                    val progress = if (scan.total > 0) scan.processed.toFloat() / scan.total else 0f
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = CinemaPrimary,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Memindai video lokal (${scan.processed}/${scan.total})...",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = CinemaPrimary,
                            trackColor = CinemaProgressBg
                        )
                    }
                }
                is ScanState.Done -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF14532D))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pemindaian selesai (${scan.added} baru, ${scan.updated} diperbarui).",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = { isScanBannerDismissed = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup notifikasi",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                is ScanState.Error -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF7F1D1D))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gagal memindai: ${scan.cause.localizedMessage}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = { isScanBannerDismissed = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup notifikasi",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                else -> {}
            }
        }

        // --- YouTube-Style Category Filter Pills ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardFilter.entries.forEach { filter ->
                val selected = uiState.selectedFilter == filter
                val label = when (filter) {
                    DashboardFilter.ALL -> "Semua"
                    DashboardFilter.IN_PROGRESS -> "Sedang Ditonton"
                    DashboardFilter.UNFINISHED -> "Belum Selesai"
                    DashboardFilter.SERIES -> "Serial"
                    DashboardFilter.MOVIES -> "Film"
                    DashboardFilter.FINISHED -> "Selesai"
                }

                FilterChip(
                    selected = selected,
                    onClick = { viewModel.onFilterSelected(filter) },
                    label = { Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CinemaPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = CinemaSurfaceVariant,
                        labelColor = CinemaTextSecondary
                    ),
                    border = null,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Main Content: Grid & Continue Watching ---
        if (uiState.collections.isEmpty() && uiState.continueWatching.isEmpty()) {
            EmptyLibraryView(
                isScanning = uiState.scanState is ScanState.Scanning,
                onRescan = { viewModel.requestRescan() }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Section: Lanjutkan Menonton (Netflix Style)
                if (uiState.continueWatching.isNotEmpty() && uiState.selectedFilter == DashboardFilter.ALL) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Text(
                                text = "LANJUTKAN MENONTON",
                                color = CinemaTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(end = 16.dp)
                            ) {
                                items(uiState.continueWatching, key = { it.id }) { ep ->
                                    ContinueWatchingCard(
                                        episode = ep,
                                        onClick = { onEpisodePlayClick(ep.id, ep.seriesId) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "SEMUA KOLEKSI (${uiState.collections.size})",
                                color = CinemaTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                }

                // Grid items: Series & Movies Cards
                items(uiState.collections, key = { it.id }) { series ->
                    SeriesCard(
                        series = series,
                        onClick = { onSeriesClick(series.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    episode: EpisodeRow,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                VideoThumbnail(
                    posterUrl = null,
                    mediaStoreId = episode.mediaStoreId,
                    modifier = Modifier.fillMaxSize()
                )

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0x99000000), CircleShape)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Putar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Episode progress bar at bottom of card
                val resumeMs = episode.resumePositionMs ?: 0L
                val progress = if (episode.durationMs > 0) resumeMs.toFloat() / episode.durationMs else 0f
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

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = episode.displayName,
                    color = CinemaTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val epText = if (episode.episodeNumber != null) "Eps ${episode.episodeNumber}" else "Movie"
                Text(
                    text = "$epText • Sisa ${(episode.durationMs - (episode.resumePositionMs ?: 0)) / 60000} mnt",
                    color = CinemaTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SeriesCard(
    series: SeriesSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
            ) {
                VideoThumbnail(
                    posterUrl = series.posterUrl,
                    mediaStoreId = series.coverMediaStoreId,
                    modifier = Modifier.fillMaxSize()
                )

                // Badge top right (Movie or Episodes)
                val badgeText = if (series.isMovie) "FILM" else "S${series.seasonNumber} • ${series.episodeCount} Eps"
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Completion progress bar
                val ratio = if (series.episodeCount > 0) series.finishedCount.toFloat() / series.episodeCount else 0f
                if (ratio > 0f) {
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = if (series.isFinished) Color(0xFF22C55E) else CinemaPrimary,
                        trackColor = CinemaProgressBg
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = series.displayTitle,
                    color = CinemaTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (series.isFinished) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    val subtitle = if (series.isFinished) {
                        "Selesai ditonton"
                    } else if (series.finishedCount > 0) {
                        "${series.finishedCount}/${series.episodeCount} ditonton"
                    } else {
                        "${series.episodeCount} episode"
                    }
                    Text(
                        text = subtitle,
                        color = if (series.isFinished) Color(0xFF22C55E) else CinemaTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryView(
    isScanning: Boolean,
    onRescan: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isScanning) Icons.Default.Refresh else Icons.Default.Movie,
                contentDescription = null,
                tint = CinemaPrimary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isScanning) "Sedang Memindai Penyimpanan..." else "Belum Ada Video Ditemukan",
                color = CinemaTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isScanning) {
                    "Mencari file video di folder Download, Movies, Video, dan DCIM..."
                } else {
                    "Simpan file serial atau film (.mp4, .mkv, .webm) di folder Download, Movies, atau Video."
                },
                color = CinemaTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRescan,
                enabled = !isScanning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaPrimary,
                    disabledContainerColor = CinemaSurfaceVariant
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sedang Memindai...", color = Color.White)
                } else {
                    Text(text = "Pindai Penyimpanan", color = Color.White)
                }
            }
        }
    }
}
