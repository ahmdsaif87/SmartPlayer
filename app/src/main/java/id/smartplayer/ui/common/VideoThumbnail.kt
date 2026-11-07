package id.smartplayer.ui.common

import android.content.ContentUris
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.smartplayer.ui.theme.CinemaSurfaceVariant
import id.smartplayer.ui.theme.CinemaTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun VideoThumbnail(
    posterUrl: String?,
    mediaStoreId: Long?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var localBitmap by remember(mediaStoreId) { mutableStateOf<Bitmap?>(null) }

    if (!posterUrl.isNullOrBlank()) {
        AsyncImage(
            model = posterUrl,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )
    } else if (mediaStoreId != null && mediaStoreId > 0) {
        LaunchedEffect(mediaStoreId) {
            withContext(Dispatchers.IO) {
                try {
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        mediaStoreId
                    )
                    val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        context.contentResolver.loadThumbnail(uri, Size(360, 240), null)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Video.Thumbnails.getThumbnail(
                            context.contentResolver,
                            mediaStoreId,
                            MediaStore.Video.Thumbnails.MINI_KIND,
                            null
                        )
                    }
                    localBitmap = bmp
                } catch (_: Throwable) {
                    // Fallback to placeholder
                }
            }
        }

        if (localBitmap != null) {
            Image(
                bitmap = localBitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = contentScale,
                modifier = modifier
            )
        } else {
            PlaceholderThumbnail(modifier)
        }
    } else {
        PlaceholderThumbnail(modifier)
    }
}

@Composable
private fun PlaceholderThumbnail(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(CinemaSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "▶",
            color = CinemaTextSecondary,
            fontSize = 24.sp
        )
    }
}
