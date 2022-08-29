package app.simple.inure.themes.data

import androidx.annotation.ColorInt

data class TextViewTheme(
        @ColorInt
        var headingTextColor: Int,

        @ColorInt
        var primaryTextColor: Int,

        @ColorInt
        var secondaryTextColor: Int,

        @ColorInt
        val tertiaryTextColor: Int,

        @ColorInt
        val quaternaryTextColor: Int,
)