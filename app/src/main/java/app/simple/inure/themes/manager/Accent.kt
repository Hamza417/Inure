package app.simple.inure.themes.manager

import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.data.AccentTheme

enum class Accent(val accentTheme: AccentTheme) {
    INURE(
            accentTheme = AccentTheme(
                    accentColor = AppearancePreferences.getAccentColor(),
                    accentColorLight = AppearancePreferences.getAccentColorLight())
    )
}