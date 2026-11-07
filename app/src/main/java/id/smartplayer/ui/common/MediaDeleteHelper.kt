package id.smartplayer.ui.common

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest

object MediaDeleteHelper {

    fun buildDeleteIntentSender(
        context: Context,
        uris: List<Uri>
    ): IntentSenderRequest? {
        if (uris.isEmpty()) return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
            IntentSenderRequest.Builder(pendingIntent.intentSender).build()
        } else {
            null
        }
    }

    fun deleteDirectly(
        context: Context,
        uris: List<Uri>
    ): Boolean {
        var allSuccess = true
        for (uri in uris) {
            try {
                val count = context.contentResolver.delete(uri, null, null)
                if (count <= 0) {
                    allSuccess = false
                }
            } catch (e: Exception) {
                allSuccess = false
            }
        }
        return allSuccess
    }
}
