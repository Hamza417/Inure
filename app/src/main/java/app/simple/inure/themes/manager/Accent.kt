package app.simple.inure.themes.manager

import app.simple.inure.R
import app.simple.inure.themes.data.AccentTheme

enum class Accent(val accentTheme: AccentTheme) {
    // Default Accent
    INURE(
            accentTheme = AccentTheme(
                    accentColor = R.color.inure,
                    accentColorLight = R.color.inure_light,
            )
    )
}