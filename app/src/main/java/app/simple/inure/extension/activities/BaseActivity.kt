package app.simple.inure.extension.activities

import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import android.view.WindowManager
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

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                                       .detectLeakedClosableObjects()
                                       .penaltyLog()
                                       .build())

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
            ContextCompat.getColor(baseContext, R.color.yellow) -> {
                setTheme(R.style.Yellow)
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
            ContextCompat.getColor(baseContext, R.color.indian_red) -> {
                setTheme(R.style.IndianRed)
            }
            ContextCompat.getColor(baseContext, R.color.light_coral) -> {
                setTheme(R.style.LightCoral)
            }
            ContextCompat.getColor(baseContext, R.color.pink_flare) -> {
                setTheme(R.style.PinkFlare)
            }
            ContextCompat.getColor(baseContext, R.color.makeup_tan) -> {
                setTheme(R.style.MakeupTan)
            }
            ContextCompat.getColor(baseContext, R.color.egg_yellow) -> {
                setTheme(R.style.EggYellow)
            }
            ContextCompat.getColor(baseContext, R.color.medium_green) -> {
                setTheme(R.style.MediumGreen)
            }
            ContextCompat.getColor(baseContext, R.color.olive) -> {
                setTheme(R.style.Olive)
            }
            ContextCompat.getColor(baseContext, R.color.copperfield) -> {
                setTheme(R.style.Copperfield)
            }
            ContextCompat.getColor(baseContext, R.color.mineral_green) -> {
                setTheme(R.style.MineralGreen)
            }
            ContextCompat.getColor(baseContext, R.color.lochinvar) -> {
                setTheme(R.style.Lochinvar)
            }
            ContextCompat.getColor(baseContext, R.color.beach_grey) -> {
                setTheme(R.style.BeachGrey)
            }
            ContextCompat.getColor(baseContext, R.color.cashmere) -> {
                setTheme(R.style.Cashmere)
            }
            ContextCompat.getColor(baseContext, R.color.grape) -> {
                setTheme(R.style.Grape)
            }
            else -> {
                setTheme(R.style.Inure)
                AppearancePreferences.setAccentColor(ContextCompat.getColor(baseContext, R.color.inure))
            }
        }
    }
}