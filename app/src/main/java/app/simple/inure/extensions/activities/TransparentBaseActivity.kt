package app.simple.inure.extensions.activities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import app.simple.inure.R
import app.simple.inure.dialogs.miscellaneous.Error.Companion.showError
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.themes.data.MaterialYou
import app.simple.inure.themes.data.MaterialYou.presetMaterialYouDynamicColors
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.themes.manager.ThemeUtils.setTransparentTheme
import app.simple.inure.util.ContextUtils

@SuppressLint("Registered")
open class TransparentBaseActivity : AppCompatActivity(), ThemeChangedListener {

    override fun attachBaseContext(newBaseContext: Context) {
        SharedPreferences.init(newBaseContext)
        super.attachBaseContext(ContextUtils.updateLocale(newBaseContext, ConfigurationPreferences.getAppLanguage()!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            presetMaterialYouDynamicColors()

            if (AppearancePreferences.isMaterialYouAccent()) {
                AppearancePreferences.setAccentColor(ContextCompat.getColor(baseContext, MaterialYou.MATERIAL_YOU_ACCENT_RES_ID))
            }
        }

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

        if (!DevelopmentPreferences.get(DevelopmentPreferences.DISABLE_TRANSPARENT_STATUS)) {
            makeAppFullScreen()
            // fixNavigationBarOverlap()
        }

        ThemeUtils.setAppTheme(resources)
        setTransparentTheme()
        ThemeUtils.setBarColors(resources, window)
        setNavColor()
    }

    @Suppress("Deprecation")
    private fun makeAppFullScreen() {
        window.statusBarColor = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT in 23..29) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
        } else
            WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = Color.TRANSPARENT
        }
    }

    @Suppress("DEPRECATION")
    private fun setNavColor() {
        if (AppearancePreferences.isAccentOnNavigationBar()) {
            window.navigationBarColor = theme.obtainStyledAttributes(intArrayOf(R.attr.colorAppAccent))
                .getColor(0, 0)
        }
    }

    protected fun showError(error: String) {
        try {
            supportFragmentManager.showError(error).setOnErrorCallbackListener {
                onBackPressedDispatcher.onBackPressed()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    protected fun showError(error: Throwable) {
        try {
            error.printStackTrace()
            supportFragmentManager.showError(error.stackTraceToString()).setOnErrorCallbackListener {
                onBackPressedDispatcher.onBackPressed()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        ThemeManager.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeManager.removeListener(this)
    }
}
