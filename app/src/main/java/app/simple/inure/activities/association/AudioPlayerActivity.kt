package app.simple.inure.activities.association

import android.os.Bundle
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extension.activities.TransparentBaseActivity
import app.simple.inure.ui.viewers.AudioPlayer
import app.simple.inure.util.NullSafety.isNull

class AudioPlayerActivity : TransparentBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState.isNull()) {
            kotlin.runCatching {
                AudioPlayer.newInstance(intent.data!!)
                    .show(supportFragmentManager, "audio_player")
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
}
