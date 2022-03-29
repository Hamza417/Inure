package app.simple.inure.activities.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.simple.inure.activities.association.AudioPlayerActivity
import app.simple.inure.activities.association.FullScreenAudioPlayerActivity
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.SharedPreferences

class AudioLauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferences.init(baseContext)

        if (DevelopmentPreferences.isAudioPlayerFullScreen()) {
            val intent = Intent(this, FullScreenAudioPlayerActivity::class.java)
            intent.data = this.intent.data
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, AudioPlayerActivity::class.java)
            intent.data = this.intent.data
            startActivity(intent)
            finish()
        }
    }
}