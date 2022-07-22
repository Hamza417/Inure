package app.simple.inure.decorations.typeface

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ColorUtils
import app.simple.inure.util.ColorUtils.animateColorChange
import app.simple.inure.util.TextViewUtils.setDrawableTint
import app.simple.inure.util.ThemeUtils
import app.simple.inure.util.TypeFace
import top.defaults.drawabletoolbox.DrawableBuilder

open class TypeFaceEditText : AppCompatEditText, ThemeChangedListener {

    private var typedArray: TypedArray
    private var colorMode: Int = 1

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.TypeFaceTextView, 0, 0)
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
        typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.TypeFaceTextView, 0, 0)
        init()
    }

    fun init() {
        typeface = TypeFace.getTypeFace(AppearancePreferences.getAppFont(), typedArray.getInt(R.styleable.TypeFaceTextView_appFontStyle, -1), context)
        colorMode = typedArray.getInt(R.styleable.TypeFaceTextView_textColorStyle, 1)
        setHighlightColor()
        setTextColor(colorMode, false)
        setCursorDrawable()
        setHintTextColor(ThemeManager.theme.textViewTheme.tertiaryTextColor)
        setDrawableTint(ThemeManager.theme.iconTheme.secondaryIconColor)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ThemeManager.addListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ThemeManager.removeListener(this)
    }

    override fun onThemeChanged(theme: Theme, animate: Boolean) {
        setTextColor(colorMode, animate)
        setHighlightColor()
    }

    private fun setTextColor(mode: Int, animate: Boolean) {
        if (animate) {
            when (mode) {
                0 -> this.animateColorChange(ThemeManager.theme.textViewTheme.headingTextColor)
                1 -> this.animateColorChange(ThemeManager.theme.textViewTheme.primaryTextColor)
                2 -> this.animateColorChange(ThemeManager.theme.textViewTheme.secondaryTextColor)
                3 -> this.animateColorChange(ThemeManager.theme.textViewTheme.tertiaryTextColor)
                4 -> this.animateColorChange(ThemeManager.theme.textViewTheme.quaternaryTextColor)
                5 -> this.animateColorChange(AppearancePreferences.getAccentColor())
            }
        } else {
            when (mode) {
                0 -> setTextColor(ThemeManager.theme.textViewTheme.headingTextColor)
                1 -> setTextColor(ThemeManager.theme.textViewTheme.primaryTextColor)
                2 -> setTextColor(ThemeManager.theme.textViewTheme.secondaryTextColor)
                3 -> setTextColor(ThemeManager.theme.textViewTheme.tertiaryTextColor)
                4 -> setTextColor(ThemeManager.theme.textViewTheme.quaternaryTextColor)
                5 -> setTextColor(AppearancePreferences.getAccentColor())
            }
        }
    }

    private fun setCursorDrawable() {
        textCursorDrawable = DrawableBuilder()
            .rectangle()
            .width(resources.getDimensionPixelOffset(R.dimen.cursor_width))
            .ripple(false)
            .strokeWidth(0)
            .solidColor(AppearancePreferences.getAccentColor())
            .build()
    }

    private fun setHighlightColor() {
        highlightColor = if (ThemeUtils.isNightMode(resources)) {
            ColorUtils.lightenColor(Color.DKGRAY, 0.2F)
        } else {
            ColorUtils.lightenColor(Color.GRAY)
        }
    }
}
