package app.simple.inure.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.simple.inure.R
import app.simple.inure.ui.Apps

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val allApps = supportFragmentManager.findFragmentByTag("all_apps") ?: Apps.newInstance()

        supportFragmentManager.beginTransaction().replace(R.id.app_container, allApps, "all_apps")
            .commit()
    }
}
