package app.simple.inure.activities.association

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import app.simple.inure.R
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.ui.association.Text
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.NullSafety.isNull

class TextViewerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState.isNull()) {
            if (hasAppPath().invert()) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.app_container, Text.newInstance())
                    .commit()
            } else {
                showWarning("ERR: illegal action detected.")
            }
        }
    }

    @SuppressLint("SdCardPath")
    private fun hasAppPath(): Boolean {
        if (intent.data.isNotNull()) {
            val appDataPath = "/data/data/$packageName/"
            val normalizedIntentPath = intent.data?.path?.replace("//+".toRegex(), "/") // Normalize multiple slashes
            Log.d("TAG", "hasAppPath: $appDataPath")
            Log.d("TAG", "hasAppPath: $normalizedIntentPath")
            return normalizedIntentPath?.contains(appDataPath) == true
        }

        return false
    }
}
