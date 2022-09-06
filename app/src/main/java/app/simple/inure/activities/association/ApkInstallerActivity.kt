package app.simple.inure.activities.association

import android.os.Bundle
import app.simple.inure.R
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.ui.installer.Installer
import app.simple.inure.util.NullSafety.isNull

class ApkInstallerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState.isNull()) {
            kotlin.runCatching {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.app_container, Installer.newInstance(intent?.data!!), "installer")
                    .commit()
            }.getOrElse {
                showError(it.stackTraceToString())
            }
        }
    }
}