package app.simple.inure.activities.app

import android.app.Activity
import android.app.AppOpsManager
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.AppOpsManagerCompat.MODE_ALLOWED
import app.simple.inure.R
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.ui.launcher.SplashScreen
import app.simple.inure.util.NullSafety.isNull


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                    .commit()
        }
    }
}
