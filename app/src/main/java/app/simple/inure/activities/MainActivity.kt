package app.simple.inure.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.Fade
import app.simple.inure.R
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.ui.app.Apps

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SharedPreferences.init(applicationContext)

        val allApps = supportFragmentManager.findFragmentByTag("all_apps") ?: Apps.newInstance()

        allApps.exitTransition = Fade()
        allApps.sharedElementEnterTransition = DetailsTransitionArc()
        allApps.enterTransition = Fade()
        allApps.sharedElementReturnTransition = DetailsTransitionArc()

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.app_container, allApps, "all_apps")
            .commit()
    }
}
