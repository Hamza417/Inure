package app.simple.inure.activities.association

import android.os.Bundle
import app.simple.inure.R
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.ui.viewers.AudioPlayerPager
import app.simple.inure.util.NullSafety.isNull

class FelicityPlayerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.app_container, AudioPlayerPager
                    .newInstance(MusicPreferences.getMusicPosition(),
                                 MusicPreferences.getFromSearch()))
                .commit()
        }
    }
}