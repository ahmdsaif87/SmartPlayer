package id.smartplayer.data.repo

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smartplayer_prefs", Context.MODE_PRIVATE)

    private val _apiKey = MutableStateFlow(prefs.getString(KEY_API_KEY, "") ?: "")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _syncOnlyWifi = MutableStateFlow(prefs.getBoolean(KEY_SYNC_ONLY_WIFI, true))
    val syncOnlyWifi: StateFlow<Boolean> = _syncOnlyWifi.asStateFlow()

    private val _autoEnrich = MutableStateFlow(prefs.getBoolean(KEY_AUTO_ENRICH, true))
    val autoEnrich: StateFlow<Boolean> = _autoEnrich.asStateFlow()

    fun setApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key.trim()).apply()
        _apiKey.value = key.trim()
    }

    fun setSyncOnlyWifi(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYNC_ONLY_WIFI, enabled).apply()
        _syncOnlyWifi.value = enabled
    }

    fun setAutoEnrich(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_ENRICH, enabled).apply()
        _autoEnrich.value = enabled
    }

    companion object {
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_SYNC_ONLY_WIFI = "sync_only_wifi"
        private const val KEY_AUTO_ENRICH = "auto_enrich_metadata"
    }
}
