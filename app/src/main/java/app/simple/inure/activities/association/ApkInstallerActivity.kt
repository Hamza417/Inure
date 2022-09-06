package app.simple.inure.activities.association

import android.os.Bundle
import app.simple.inure.dialogs.association.Installer
import app.simple.inure.extensions.activities.TransparentBaseActivity
import app.simple.inure.util.NullSafety.isNull

class ApkInstallerActivity : TransparentBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState.isNull()) {
            kotlin.runCatching {
                Installer.newInstance(intent?.data!!)
                    .show(supportFragmentManager, "installer")
            }.getOrElse {
                showError(it.stackTraceToString())
            }
        }
    }
}