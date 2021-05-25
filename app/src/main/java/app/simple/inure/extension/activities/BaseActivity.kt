package app.simple.inure.extension.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.util.ThemeSetter

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {

        SharedPreferences.init(newBase)

        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Sets window flags for keeping the screen on
         */
        if (ConfigurationPreferences.isKeepScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setTheme()
        ThemeSetter.setAppTheme(AppearancePreferences.getAppTheme())
    }

    private fun setTheme() {
        when (AppearancePreferences.getAccentColor()) {
            ContextCompat.getColor(baseContext, R.color.inure) -> {
                setTheme(R.style.Inure)
            }
            ContextCompat.getColor(baseContext, R.color.blue) -> {
                setTheme(R.style.Blue)
            }
            ContextCompat.getColor(baseContext, R.color.blueGrey) -> {
                setTheme(R.style.BlueGrey)
            }
            ContextCompat.getColor(baseContext, R.color.darkBlue) -> {
                setTheme(R.style.DarkBlue)
            }
            ContextCompat.getColor(baseContext, R.color.red) -> {
                setTheme(R.style.Red)
            }
            ContextCompat.getColor(baseContext, R.color.green) -> {
                setTheme(R.style.Green)
            }
            ContextCompat.getColor(baseContext, R.color.orange) -> {
                setTheme(R.style.Orange)
            }
            ContextCompat.getColor(baseContext, R.color.purple) -> {
                setTheme(R.style.Purple)
            }
            ContextCompat.getColor(baseContext, R.color.brown) -> {
                setTheme(R.style.Brown)
            }
            ContextCompat.getColor(baseContext, R.color.caribbeanGreen) -> {
                setTheme(R.style.CaribbeanGreen)
            }
            ContextCompat.getColor(baseContext, R.color.persianGreen) -> {
                setTheme(R.style.PersianGreen)
            }
            ContextCompat.getColor(baseContext, R.color.amaranth) -> {
                setTheme(R.style.Amaranth)
            }
            else -> {
                setTheme(R.style.Inure)
                AppearancePreferences.setAccentColor(ContextCompat.getColor(baseContext, R.color.inure))
            }
        }
    }
}