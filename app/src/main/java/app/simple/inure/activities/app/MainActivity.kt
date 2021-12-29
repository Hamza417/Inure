package app.simple.inure.activities.app

import android.os.Bundle
import android.widget.Toast
import app.simple.inure.R
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.ui.launcher.SplashScreen
import app.simple.inure.util.CalendarUtils
import app.simple.inure.util.NullSafety.isNull
import java.time.ZonedDateTime
import java.util.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AndroidBug5497Workaround.assistActivity(this)

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                .commit()
        }
    }

    private fun setExpiryStamp() {
        val expiryDate = Calendar.getInstance()

        expiryDate.clear()
        expiryDate.set(2021, Calendar.DECEMBER, 31)
        expiryDate.timeZone = TimeZone.getTimeZone(ZonedDateTime.now().zone.id)

        if (CalendarUtils.isToday(expiryDate)) {
            Toast.makeText(applicationContext, "Application Expired!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
