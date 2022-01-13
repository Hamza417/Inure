package app.simple.inure.util

import android.content.res.Configuration
import android.content.res.Resources
import android.view.Window
import androidx.core.view.WindowInsetsControllerCompat
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import java.util.*

object ThemeUtils {
    fun setAppTheme(resources: Resources) {
        when (AppearancePreferences.getTheme()) {
            ThemeConstants.LIGHT_THEME -> {
                ThemeManager.theme = Theme.LIGHT
            }
            ThemeConstants.DARK_THEME -> {
                ThemeManager.theme = Theme.DARK
            }
            ThemeConstants.AMOLED -> {
                ThemeManager.theme = Theme.AMOLED
            }
            ThemeConstants.FOLLOW_SYSTEM -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        when (AppearancePreferences.getLastDarkTheme()) {
                            ThemeConstants.DARK_THEME -> {
                                ThemeManager.theme = Theme.DARK
                            }
                            ThemeConstants.AMOLED -> {
                                ThemeManager.theme = Theme.AMOLED
                            }
                        }
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        ThemeManager.theme = Theme.LIGHT
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        ThemeManager.theme = Theme.LIGHT
                    }
                }
            }
            ThemeConstants.DAY_NIGHT -> {
                val calendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (calendar < 7 || calendar > 18) {
                    when (AppearancePreferences.getLastDarkTheme()) {
                        ThemeConstants.DARK_THEME -> {
                            ThemeManager.theme = Theme.DARK
                        }
                        ThemeConstants.AMOLED -> {
                            ThemeManager.theme = Theme.AMOLED
                        }
                    }
                } else if (calendar < 18 || calendar > 6) {
                    ThemeManager.theme = Theme.LIGHT
                }
            }
        }
    }

    fun setBarColors(resources: Resources, window: Window) {
        when (AppearancePreferences.getTheme()) {
            ThemeConstants.LIGHT_THEME -> {
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
            }
            ThemeConstants.DARK_THEME -> {
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
            }
            ThemeConstants.AMOLED -> {
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
            }
            ThemeConstants.FOLLOW_SYSTEM -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
                        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
                        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
                        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
                    }
                }
            }
            ThemeConstants.DAY_NIGHT -> {
                val calendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (calendar < 7 || calendar > 18) {
                    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
                    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
                } else if (calendar < 18 || calendar > 6) {
                    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
                    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
                }
            }
        }
    }
}