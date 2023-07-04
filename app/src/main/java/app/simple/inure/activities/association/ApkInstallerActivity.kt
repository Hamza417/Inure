package app.simple.inure.activities.association

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.ui.installer.Installer
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ParcelUtils.serializable
import java.io.File

class ApkInstallerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState.isNull()) {
            kotlin.runCatching {
                if (intent.extras?.serializable<File>(BundleConstants.file).isNotNull()) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Installer.newInstance(intent.extras?.serializable<File>(BundleConstants.file)!!), "installer")
                        .commit()
                } else {
                    if (intent?.action == Intent.ACTION_SEND) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_container, Installer.newInstance(intent.parcelable<Uri>(Intent.EXTRA_STREAM)!!), "installer")
                            .commit()
                    } else {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_container, Installer.newInstance(intent!!.data!!), "installer")
                            .commit()
                    }
                }
            }.getOrElse {
                showError(it.stackTraceToString())
            }
        }
    }

    private fun clearInstallerCache() {
        kotlin.runCatching {
            if (File(applicationContext.cacheDir.path + "/installer_cache/").deleteRecursively()) {
                Log.d(javaClass.name, "Installer cache cleared")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearInstallerCache()
    }
}