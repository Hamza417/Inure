package app.simple.inure.activities.association

import android.os.Bundle
import app.simple.inure.extension.activities.BaseActivity

class XmlAssociationActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println(intent.action)
    }
}