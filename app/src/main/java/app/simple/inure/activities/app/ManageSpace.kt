package app.simple.inure.activities.app

import android.app.ActivityManager
import android.os.Bundle
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.dialogs.app.Sure
import app.simple.inure.extensions.activities.BaseActivity

class ManageSpace : BaseActivity() {

    private lateinit var clearData: DynamicRippleTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_space)

        clearData = findViewById(R.id.clear_app_data)

        clearData.setOnClickListener {
            clearAppData()
        }
    }

    private fun clearAppData() {
        val p = Sure.newInstance()
        p.setOnSureCallbackListener(object : Sure.Companion.SureCallbacks {
            override fun onSure() {
                (applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                    .clearApplicationUserData() // note: it has a return value!
            }
        })

        p.show(supportFragmentManager, "sure")
    }
}