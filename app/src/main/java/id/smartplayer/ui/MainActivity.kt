package id.smartplayer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import id.smartplayer.SmartPlayerApp
import id.smartplayer.ui.common.PermissionGate
import id.smartplayer.ui.nav.AppNavGraph
import id.smartplayer.ui.theme.CinemaBackground
import id.smartplayer.ui.theme.SmartPlayerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as SmartPlayerApp
        val container = app.container

        setContent {
            SmartPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CinemaBackground
                ) {
                    PermissionGate(
                        onPermissionGranted = {
                            container.libraryRepository.requestScan()
                        }
                    ) {
                        AppNavGraph()
                    }
                }
            }
        }
    }
}
