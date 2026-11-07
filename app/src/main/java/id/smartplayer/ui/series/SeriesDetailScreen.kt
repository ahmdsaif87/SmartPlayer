package id.smartplayer.ui.series

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun SeriesDetailScreen(
    viewModel: SeriesDetailViewModel,
    onBackClick: () -> Unit,
    onPlayClick: (Long, Long) -> Unit, // episodeId, seriesId
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBackground)
    ) {
        // --- Hero Header with Gradient Scrim ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                VideoThumbnail(
                    posterUrl = series.backdropUrl ?: series.posterUrl,
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

                // Back button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color(0x66000000), RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
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

                    if (!series.genres.isNullOrBlank()) {
                        Text(text = "•", color = CinemaTextSecondary)
                        Text(
                            text = series.genres,
                            color = CinemaSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // AI Synopsis
                if (!series.synopsis.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = series.synopsis,
                        color = CinemaTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Netflix-Style Primary CTA Button
                if (resumeTarget != null) {
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

                Text(
                    text = "DAFTAR EPISODE",
                    color = CinemaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // --- Flat Episode List ---
        items(episodes, key = { it.id }) { ep ->
            EpisodeItemRow(
                episode = ep,
                onClick = { onPlayClick(ep.id, series.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EpisodeItemRow(
    episode: EpisodeRow,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
        }
    }
}
