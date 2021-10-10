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

open class TransparentBaseActivity : AppCompatActivity() {

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
                setTheme(R.style.Inure_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.blue) -> {
                setTheme(R.style.Blue_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.blueGrey) -> {
                setTheme(R.style.BlueGrey_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.darkBlue) -> {
                setTheme(R.style.DarkBlue_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.red) -> {
                setTheme(R.style.Red_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.green) -> {
                setTheme(R.style.Green_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.orange) -> {
                setTheme(R.style.Orange_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.purple) -> {
                setTheme(R.style.Purple_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.yellow) -> {
                setTheme(R.style.Yellow_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.caribbeanGreen) -> {
                setTheme(R.style.CaribbeanGreen_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.persianGreen) -> {
                setTheme(R.style.PersianGreen_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.amaranth) -> {
                setTheme(R.style.Amaranth_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.indian_red) -> {
                setTheme(R.style.IndianRed_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.light_coral) -> {
                setTheme(R.style.LightCoral_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.pink_flare) -> {
                setTheme(R.style.PinkFlare_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.makeup_tan) -> {
                setTheme(R.style.MakeupTan_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.egg_yellow) -> {
                setTheme(R.style.EggYellow_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.medium_green) -> {
                setTheme(R.style.MediumGreen_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.olive) -> {
                setTheme(R.style.Olive_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.copperfield) -> {
                setTheme(R.style.Copperfield_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.mineral_green) -> {
                setTheme(R.style.MineralGreen_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.lochinvar) -> {
                setTheme(R.style.Lochinvar_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.beach_grey) -> {
                setTheme(R.style.BeachGrey_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.cashmere) -> {
                setTheme(R.style.Cashmere_Transparent)
            }
            ContextCompat.getColor(baseContext, R.color.grape) -> {
                setTheme(R.style.Grape_Transparent)
            }
            else -> {
                setTheme(R.style.Inure_Transparent)
                AppearancePreferences.setAccentColor(ContextCompat.getColor(baseContext, R.color.inure))
            }
        }
    }
}