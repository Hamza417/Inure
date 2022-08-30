package app.simple.inure.constants

import android.os.Build
import androidx.annotation.RequiresApi

object ThemeConstants {
    const val LIGHT_THEME = 0
    const val DARK_THEME = 1
    const val AMOLED = 2
    const val FOLLOW_SYSTEM = 3
    const val DAY_NIGHT = 4
    const val SLATE = 5
    const val HIGH_CONTRAST = 6
    const val SOAPSTONE = 7
    const val OIL = 8

    @RequiresApi(Build.VERSION_CODES.S)
    const val MATERIAL_YOU = 9
}