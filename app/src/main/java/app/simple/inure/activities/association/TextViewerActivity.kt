package app.simple.inure.activities.association

import android.os.Bundle
import app.simple.inure.R
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.ui.association.Text
import app.simple.inure.util.AppUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.IntentHelper.hasAppPath
import app.simple.inure.util.NullSafety.isNull

class TextViewerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (AppUtils.isPlayFlavor()) {
            showWarning("ERR: text viewer has been removed from play builds.")
            return
        }

        if (savedInstanceState.isNull()) {
            if (intent.hasAppPath(packageName).invert()) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.app_container, Text.newInstance())
                    .commit()
            } else {
                showWarning("ERR: illegal action detected.")
            }
        }
    }
}
