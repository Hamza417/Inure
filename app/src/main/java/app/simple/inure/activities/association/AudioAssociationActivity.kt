package app.simple.inure.activities.association

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.ui.viewers.AudioPlayer
import app.simple.inure.util.NullSafety.isNull


class AudioAssociationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPreferences.init(this)

        /**
         * Sets window flags for keeping the screen on
         */
        if (ConfigurationPreferences.isKeepScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if(savedInstanceState.isNull()) {
            AudioPlayer.newInstance(intent.data!!)
                    .show(supportFragmentManager, "audio_player")
        }
    }
}