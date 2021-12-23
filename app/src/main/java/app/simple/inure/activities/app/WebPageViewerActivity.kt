package app.simple.inure.activities.app

import android.os.Bundle
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.ui.panels.WebPage
import app.simple.inure.util.ConditionUtils.isNotNull
import app.simple.inure.util.ConditionUtils.isNull

class WebPageViewerActivity : BaseActivity() {

    private var source: String = "null"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        source = if (intent.extras.isNotNull()) {
            intent.extras!!.getString(BundleConstants.webPage, "null")
        } else {
            "null"
        }

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.app_container, WebPage.newInstance(source), "web_page")
                .commit()
        }
    }
}