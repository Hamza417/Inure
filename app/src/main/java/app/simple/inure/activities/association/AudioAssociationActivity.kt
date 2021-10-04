package app.simple.inure.activities.association

import android.os.Bundle
import app.simple.inure.extension.activities.TransparentBaseActivity
import app.simple.inure.ui.viewers.AudioPlayer
import app.simple.inure.util.NullSafety.isNull

class AudioAssociationActivity : TransparentBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState.isNull()) {
            AudioPlayer.newInstance(intent.data!!)
                    .show(supportFragmentManager, "audio_player")
        }
    }
}
