package app.simple.inure.activities.association

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.ui.panels.Installer
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
                        .replace(R.id.app_container, Installer.newInstance(intent.extras?.serializable<File>(BundleConstants.file)!!), Installer.TAG)
                        .commit()
                } else {
                    if (intent?.action == Intent.ACTION_SEND) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_container, Installer.newInstance(intent.parcelable<Uri>(Intent.EXTRA_STREAM)!!), Installer.TAG)
                            .commit()
                    } else {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_container, Installer.newInstance(intent!!.data!!), Installer.TAG)
                            .commit()
                    }
                }
            }.getOrElse {
                showError(it.stackTraceToString())
            }
        }
    }
}
