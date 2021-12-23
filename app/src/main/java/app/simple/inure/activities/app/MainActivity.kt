package app.simple.inure.activities.app

import android.os.Bundle
import app.simple.inure.R
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.ui.launcher.SplashScreen
import app.simple.inure.util.NullSafety.isNull

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        // AndroidBug5497Workaround.assistActivity(this)

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                    .commit()
        }
    }
}
