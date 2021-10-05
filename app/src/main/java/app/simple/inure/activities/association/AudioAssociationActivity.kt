package app.simple.inure.activities.association

import android.os.Bundle
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.activities.TransparentBaseActivity
import app.simple.inure.ui.viewers.AudioPlayer
import app.simple.inure.util.NullSafety.isNull

class AudioAssociationActivity : TransparentBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState.isNull()) {
            kotlin.runCatching {
                AudioPlayer.newInstance(intent.data!!)
                        .show(supportFragmentManager, "audio_player")
            }.getOrElse {
                val e = ErrorPopup.newInstance(it.stackTraceToString())
                e.show(supportFragmentManager, "error_dialog")
                e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                    override fun onDismiss() {
                        onBackPressed()
                    }
                })
            }
        }
    }
}
