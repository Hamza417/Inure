package app.simple.inure.util

import android.content.res.Configuration
import android.content.res.Resources
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import java.util.*

object ThemeUtils {
    fun setAppTheme(resources: Resources) {
        when (AppearancePreferences.getTheme()) {
            ThemeManager.light -> {
                ThemeManager.theme = Theme.LIGHT
            }
            ThemeManager.dark -> {
                ThemeManager.theme = Theme.DARK
            }
            ThemeManager.amoled -> {
                ThemeManager.dark
            }
            ThemeManager.followSystem -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        ThemeManager.theme = Theme.DARK
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        ThemeManager.theme = Theme.LIGHT
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        ThemeManager.theme = Theme.LIGHT
                    }
                }
            }
            ThemeManager.dayNight -> {
                val calendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (calendar < 7 || calendar > 18) {
                    ThemeManager.theme = Theme.DARK
                } else if (calendar < 18 || calendar > 6) {
                    ThemeManager.theme = Theme.LIGHT
                }
            }
        }
    }
}