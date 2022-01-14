package app.simple.inure.themes.data

import androidx.annotation.ColorInt

data class ViewGroupTheme(
        @ColorInt
        val background: Int,

        @ColorInt
        val viewerBackground: Int,

        @ColorInt
        val highlightBackground: Int,

        @ColorInt
        val selectedBackground: Int,

        @ColorInt
        val dividerBackground: Int
)