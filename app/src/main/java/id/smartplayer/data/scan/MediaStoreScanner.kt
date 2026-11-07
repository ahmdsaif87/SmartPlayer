package id.smartplayer.data.scan

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreScanner(private val context: Context) {

    private val contentResolver: ContentResolver get() = context.contentResolver

    suspend fun scan(): List<RawMediaFile> = withContext(Dispatchers.IO) {
        val result = mutableListOf<RawMediaFile>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATA
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Video.Media.RELATIVE_PATH)
                add(MediaStore.Video.Media.IS_PENDING)
            }
        }.toTypedArray()

        val selection: String
        val selectionArgs: Array<String>

        val minSize = (1 * 1024 * 1024).toString() // 1 MB (ensures episodes aren't dropped)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = """
                (
                  relative_path LIKE 'Download/%'
                  OR relative_path LIKE 'Movies/%'
                  OR relative_path LIKE 'Video/%'
                  OR relative_path LIKE 'Videos/%'
                  OR relative_path LIKE 'DCIM/%'
                )
                AND (is_pending IS NULL OR is_pending = 0)
                AND _size >= ?
                AND (
                  mime_type IN ('video/mp4', 'video/x-matroska', 'video/matroska', 'video/webm')
                  OR _display_name LIKE '%.mkv' 
                  OR _display_name LIKE '%.mp4' 
                  OR _display_name LIKE '%.webm'
                )
            """.trimIndent()
            selectionArgs = arrayOf(minSize)
        } else {
            selection = """
                (
                  _data LIKE '%/Download/%'
                  OR _data LIKE '%/Movies/%'
                  OR _data LIKE '%/Video/%'
                  OR _data LIKE '%/Videos/%'
                  OR _data LIKE '%/DCIM/%'
                )
                AND _size >= ?
                AND (
                  mime_type IN ('video/mp4', 'video/x-matroska', 'video/matroska', 'video/webm')
                  OR _display_name LIKE '%.mkv' 
                  OR _display_name LIKE '%.mp4' 
                  OR _display_name LIKE '%.webm'
                )
            """.trimIndent()
            selectionArgs = arrayOf(minSize)
        }

        val sortOrder = "${MediaStore.Video.Media._ID} ASC"

        try {
            contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                } else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val displayName = cursor.getString(nameCol) ?: continue
                    val size = cursor.getLong(sizeCol)
                    val duration = cursor.getLong(durationCol)
                    val dateAdded = cursor.getLong(dateAddedCol)
                    val dateModified = cursor.getLong(dateModCol)
                    val absolutePath = cursor.getString(dataCol) ?: ""
                    val relativePath = if (relPathCol >= 0) cursor.getString(relPathCol) ?: "" else ""
                    val contentUri = ContentUris.withAppendedId(collection, id).toString()

                    result.add(
                        RawMediaFile(
                            mediaStoreId = id,
                            contentUri = contentUri,
                            absolutePath = absolutePath,
                            relativePath = relativePath,
                            displayName = displayName,
                            sizeBytes = size,
                            durationMs = duration,
                            dateAddedSec = dateAdded,
                            dateModifiedSec = dateModified
                        )
                    )
                }
            }
            Log.d("SmartPlayer", "MediaStoreScanner queried successfully, found ${result.size} raw videos")
        } catch (e: Exception) {
            Log.e("SmartPlayer", "Error querying MediaStore", e)
            throw e
        }

        result
    }
}
