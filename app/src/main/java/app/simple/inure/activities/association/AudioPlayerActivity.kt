package app.simple.inure.activities.association

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.themes.manager.Theme
import app.simple.inure.ui.viewers.AudioPlayer
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ThemeUtils

class AudioPlayerActivity : BaseActivity() {

    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        if (savedInstanceState.isNull()) {
            kotlin.runCatching {
                if (intent.hasExtra(BundleConstants.audioModel)) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, AudioPlayer.newInstance(uri!!, fromActivity = true), "audio_player")
                        .commit()
                } else {
                    uri = if (intent?.action == Intent.ACTION_SEND && intent?.type?.startsWith("audio/") == true) {
                        intent.parcelable(Intent.EXTRA_STREAM)
                    } else if (intent?.action == Intent.ACTION_SEND && intent?.type == "text/plain") {
                        intent.getStringExtra(Intent.EXTRA_TEXT)?.toUri()
                    } else {
                        intent!!.data
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, AudioPlayer.newInstance(uri!!, fromActivity = true), "audio_player")
                        .commit()
                }
            }.getOrElse {
                showError(it.stackTraceToString())
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
