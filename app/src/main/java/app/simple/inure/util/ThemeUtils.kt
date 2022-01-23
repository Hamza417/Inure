package app.simple.inure.util

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
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
            ThemeConstants.SLATE -> {
                ThemeManager.theme = Theme.SLATE
            }
            ThemeConstants.HIGH_CONTRAST -> {
                ThemeManager.theme = Theme.HIGH_CONTRAST
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
                            ThemeConstants.SLATE -> {
                                ThemeManager.theme = Theme.SLATE
                            }
                            ThemeConstants.HIGH_CONTRAST -> {
                                ThemeManager.theme = Theme.HIGH_CONTRAST
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
                        ThemeConstants.SLATE -> {
                            ThemeManager.theme = Theme.SLATE
                        }
                        ThemeConstants.HIGH_CONTRAST -> {
                            ThemeManager.theme = Theme.HIGH_CONTRAST
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
                lightBars(window)
            }
            ThemeConstants.DARK_THEME,
            ThemeConstants.AMOLED,
            ThemeConstants.HIGH_CONTRAST,
            ThemeConstants.SLATE -> {
                darkBars(window)
            }
            ThemeConstants.FOLLOW_SYSTEM -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        darkBars(window)
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        lightBars(window)
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        lightBars(window)
                    }
                }
            }
            ThemeConstants.DAY_NIGHT -> {
                val calendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (calendar < 7 || calendar > 18) {
                    darkBars(window)
                } else if (calendar < 18 || calendar > 6) {
                    lightBars(window)
                }
            }
        }
    }

    private fun lightBars(window: Window) {
        setStatusAndNavColors(window)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = !AppearancePreferences.isAccentOnNavigationBar()
    }

    private fun darkBars(window: Window) {
        setStatusAndNavColors(window)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
    }

    private fun setStatusAndNavColors(window: Window) {
        if (AppearancePreferences.isTransparentStatusDisabled()) {
            window.statusBarColor = ThemeManager.theme.viewGroupTheme.background
        } else {
            window.statusBarColor = Color.TRANSPARENT
        }

        if (!AppearancePreferences.isAccentOnNavigationBar()) {
            window.navigationBarColor = ThemeManager.theme.viewGroupTheme.background
        }
    }

    fun isNightMode(resources: Resources): Boolean {
        when (AppearancePreferences.getTheme()) {
            ThemeConstants.LIGHT_THEME -> {
                return false
            }
            ThemeConstants.DARK_THEME,
            ThemeConstants.AMOLED,
            ThemeConstants.HIGH_CONTRAST,
            ThemeConstants.SLATE -> {
                return true
            }
            ThemeConstants.FOLLOW_SYSTEM -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        return true
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        return false
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        return false
                    }
                }
            }
            ThemeConstants.DAY_NIGHT -> {
                val calendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (calendar < 7 || calendar > 18) {
                    return true
                } else if (calendar < 18 || calendar > 6) {
                    return false
                }
            }
        }

        return false
    }
}