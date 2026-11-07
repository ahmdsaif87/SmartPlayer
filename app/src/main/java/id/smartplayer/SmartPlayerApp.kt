package id.smartplayer

import android.app.Application
import id.smartplayer.di.AppContainer

class SmartPlayerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
