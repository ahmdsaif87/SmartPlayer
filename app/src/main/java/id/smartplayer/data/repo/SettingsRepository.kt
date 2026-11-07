package id.smartplayer.data.repo

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smartplayer_prefs", Context.MODE_PRIVATE)

    private val _excludeCamera = MutableStateFlow(prefs.getBoolean(KEY_EXCLUDE_CAMERA, true))
    val excludeCamera: StateFlow<Boolean> = _excludeCamera.asStateFlow()

    fun setExcludeCamera(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EXCLUDE_CAMERA, enabled).apply()
        _excludeCamera.value = enabled
    }

    companion object {
        private const val KEY_EXCLUDE_CAMERA = "exclude_camera_videos"
    }
}
