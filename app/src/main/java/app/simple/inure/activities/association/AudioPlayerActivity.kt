package app.simple.inure.activities.association

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.core.net.toUri
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extensions.activities.TransparentBaseActivity
import app.simple.inure.themes.manager.Theme
import app.simple.inure.ui.viewers.AudioPlayer
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ThemeUtils

class AudioPlayerActivity : TransparentBaseActivity() {

    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState.isNull()) {
            kotlin.runCatching {
                uri = if (intent?.action == Intent.ACTION_SEND && intent?.type?.startsWith("audio/") == true) {
                    intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
                } else if (intent?.action == Intent.ACTION_SEND && intent?.type == "text/plain") {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.toUri()
                } else {
                    intent!!.data
                }

                println(uri.toString())

                AudioPlayer.newInstance(uri!!)
                    .show(supportFragmentManager, "audio_player")
            }.getOrElse {
                it.printStackTrace()
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
