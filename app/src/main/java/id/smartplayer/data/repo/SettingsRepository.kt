package id.smartplayer.data.repo

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smartplayer_prefs", Context.MODE_PRIVATE)

    companion object {
        // App-level preferences
    }
}
