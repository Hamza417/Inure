package app.simple.inure.activities

import android.app.AppOpsManager
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AppOpsManagerCompat.MODE_ALLOWED
import androidx.fragment.app.Fragment
import app.simple.inure.R
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.ui.launcher.SplashScreen
import app.simple.inure.util.NullSafety.isNull


class MainActivity : AppCompatActivity() {

    private lateinit var fragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SharedPreferences.init(applicationContext)

        if (!checkForPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        if (savedInstanceState.isNull()) {
            fragment = supportFragmentManager.findFragmentByTag("splash_screen") ?: SplashScreen.newInstance()

            supportFragmentManager.beginTransaction()
                    .replace(R.id.app_container, fragment, "splash_screen")
                    .commit()
        }
    }

    private fun checkForPermission(): Boolean {
        val appOps = baseContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), baseContext.packageName)
        } else {
            @Suppress("Deprecation")
            appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), baseContext.packageName)
        }
        return mode == MODE_ALLOWED
    }
}
