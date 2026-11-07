package id.smartplayer.ui.history

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.smartplayer.data.db.entities.HistoryItemRow
import id.smartplayer.ui.common.VideoThumbnail
import id.smartplayer.ui.theme.CinemaBackground
import id.smartplayer.ui.theme.CinemaPrimary
import id.smartplayer.ui.theme.CinemaProgressBg
import id.smartplayer.ui.theme.CinemaSurface
import id.smartplayer.ui.theme.CinemaSurfaceVariant
import id.smartplayer.ui.theme.CinemaTextPrimary
import id.smartplayer.ui.theme.CinemaTextSecondary

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit,
    onEpisodePlayClick: (Long, Long) -> Unit, // episodeId, seriesId
    modifier: Modifier = Modifier
) {
    val items by viewModel.historyItems.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBackground)
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Riwayat Menonton",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (items.isNotEmpty()) "${items.size} video terakhir diputar" else "Belum ada riwayat",
                    color = CinemaTextSecondary,
                    fontSize = 12.sp
                )
            }

            if (items.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Riwayat",
                        tint = CinemaTextSecondary
                    )
                }
            }
        }

        // --- Main Content ---
        if (items.isEmpty()) {
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
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(CinemaSurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = CinemaTextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Riwayat",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Video yang Anda putar akan tercatat otomatis di sini untuk memudahkan melanjutkan tontonan.",
                        color = CinemaTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.episodeId }) { item ->
                    HistoryItemCard(
                        item = item,
                        onClick = { onEpisodePlayClick(item.episodeId, item.seriesId) }
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = CinemaSurface,
            title = {
                Text(
                    text = "Hapus Semua Riwayat?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Semua riwayat progres tontonan akan dihapus. Video lokal tidak akan terpengaruh.",
                    color = CinemaTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Hapus", color = CinemaPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = CinemaTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun HistoryItemCard(
    item: HistoryItemRow,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 16:9 Thumbnail
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                VideoThumbnail(
                    posterUrl = null,
                    mediaStoreId = item.mediaStoreId,
                    modifier = Modifier.fillMaxSize()
                )

                // Play Icon Overlay
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x99000000), CircleShape)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Putar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Progress Bar overlay at bottom of thumbnail
                val pos = item.resumePositionMs ?: 0L
                val dur = item.durationMs
                val fraction = if (dur > 0) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f

                LinearProgressIndicator(
                    progress = { if (item.isFinished) 1f else fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = if (item.isFinished) Color(0xFF4ADE80) else CinemaPrimary,
                    trackColor = CinemaProgressBg
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.seriesTitle,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                val epLabel = if (item.episodeNumber != null) {
                    "Musim ${item.seasonNumber} • Episode ${item.episodeNumber}"
                } else {
                    item.displayName
                }
                Text(
                    text = epLabel,
                    color = CinemaTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Status: Finished or Remaining
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isFinished) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Selesai ditonton",
                            color = Color(0xFF4ADE80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        val remainingMs = (item.durationMs - (item.resumePositionMs ?: 0L)).coerceAtLeast(0L)
                        val remainingMin = (remainingMs / 60000L).coerceAtLeast(1L)
                        Text(
                            text = "Tersisa $remainingMin mnt",
                            color = CinemaPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (item.lastPlayedEpochMs > 0) {
                        Text(
                            text = " • ",
                            color = CinemaTextSecondary.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                        val timeSpan = DateUtils.getRelativeTimeSpanString(
                            item.lastPlayedEpochMs,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        ).toString()
                        Text(
                            text = timeSpan,
                            color = CinemaTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
