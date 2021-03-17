package app.simple.inure.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.ui.app.Apps
import app.simple.inure.util.NullSafety.isNull

class MainActivity : AppCompatActivity() {

    private lateinit var allApps: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SharedPreferences.init(applicationContext)

        if (savedInstanceState.isNull()) {
            allApps = supportFragmentManager.findFragmentByTag("all_apps") ?: Apps.newInstance()

            supportFragmentManager.beginTransaction()
                .replace(R.id.app_container, allApps, "all_apps")
                .commit()
        }
    }
}
