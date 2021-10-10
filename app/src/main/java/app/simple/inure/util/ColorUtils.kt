package app.simple.inure.util

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

object ColorUtils {
    @ColorInt
    fun Context.resolveAttrColor(@AttrRes attr: Int): Int {
        val a = theme.obtainStyledAttributes(intArrayOf(attr))
        val color: Int
        try {
            color = a.getColor(0, 0)
        } finally {
            a.recycle()
        }
        return color
    }

    fun changeAlpha(origColor: Int, userInputAlpha: Int): Int {
        return origColor and 0x00ffffff or (userInputAlpha shl 24)
    }

    fun Int.toHexColor(): String {
        return String.format("#%06X", 0xFFFFFF and this)
    }
}