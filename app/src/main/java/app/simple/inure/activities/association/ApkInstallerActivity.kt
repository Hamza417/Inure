package app.simple.inure.activities.association

import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import app.simple.inure.extensions.activities.BaseActivity

class ApkInstallerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println(DocumentFile.fromSingleUri(applicationContext, intent!!.data!!)?.name)
    }
}