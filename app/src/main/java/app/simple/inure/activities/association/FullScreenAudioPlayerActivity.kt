package app.simple.inure.activities.association

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.simple.inure.R
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.themes.manager.Theme
import app.simple.inure.ui.viewers.FullScreenAudioPlayer
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ThemeUtils

class FullScreenAudioPlayerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false

        if (savedInstanceState.isNull()) {
            kotlin.runCatching {
                setContentView(R.layout.activity_main)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.app_container, FullScreenAudioPlayer.newInstance(intent.data!!), "fs_audio_player")
                    .commit()
            }.getOrElse {
                val e = Error.newInstance(it.stackTraceToString())
                e.show(supportFragmentManager, "error_dialog")
                e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                    override fun onDismiss() {
                        onBackPressed()
                    }
                })
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ThemeUtils.setAppTheme(resources)
        ThemeUtils.setBarColors(resources, window)
    }

    override fun onThemeChanged(theme: Theme, animate: Boolean) {
        ThemeUtils.setBarColors(resources, window)
    }
}