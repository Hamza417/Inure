package app.simple.inure.activities.association

import android.content.Intent
import android.os.Bundle
import android.util.Log
import app.simple.inure.R
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.ui.installer.Installer
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ParcelUtils.parcelable

class ApkInstallerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState.isNull()) {
            kotlin.runCatching {
                Log.d("Installer", intent?.action.toString())
                Log.d("Installer", intent?.type.toString())
                if (intent?.action == Intent.ACTION_SEND) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Installer.newInstance(intent.parcelable(Intent.EXTRA_STREAM)!!), "installer")
                        .commit()
                } else {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Installer.newInstance(intent!!.data!!), "installer")
                        .commit()
                }
            }.getOrElse {
                showError(it.stackTraceToString())
            }
        }
    }
}