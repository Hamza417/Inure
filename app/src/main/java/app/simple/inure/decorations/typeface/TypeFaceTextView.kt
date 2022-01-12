package app.simple.inure.decorations.typeface

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences.getAppFont
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeChangedListener
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ColorUtils.animateColorChange
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.TypeFace

open class TypeFaceTextView : AppCompatTextView, ThemeChangedListener {
    private val typedArray: TypedArray
    private var colorMode: Int = 1

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.TypeFaceTextView, 0, 0)
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.TypeFaceTextView, defStyleAttr, 0)
        init()
    }

    private fun init() {
        typeface = TypeFace.getTypeFace(getAppFont(), typedArray.getInt(R.styleable.TypeFaceTextView_appFontStyle, 0), context)
        colorMode = typedArray.getInt(R.styleable.TypeFaceTextView_textColorStyle, 1)

        setTextColor(colorMode, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isSingleLine) {
                if (BehaviourPreferences.isMarqueeOn()) {
                    isSelected = true
                } else {
                    isSingleLine = false
                    ellipsize = null
                }
            }
        } else {
            if (lineCount <= 1) {
                if (BehaviourPreferences.isMarqueeOn()) {
                    isSelected = true
                } else {
                    isSingleLine = false
                    ellipsize = null
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ThemeManager.addListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ThemeManager.removeListener(this)
    }

    override fun onThemeChanged(theme: Theme) {
        setTextColor(colorMode, true)
    }

    private fun setTextColor(mode: Int, animate: Boolean) {
        if (animate) {
            when (mode) {
                0 -> this.animateColorChange(ThemeManager.theme.textViewTheme.headingTextColor)
                1 -> this.animateColorChange(ThemeManager.theme.textViewTheme.primaryTextColor)
                2 -> this.animateColorChange(ThemeManager.theme.textViewTheme.secondaryTextColor)
                3 -> this.animateColorChange(ThemeManager.theme.textViewTheme.tertiaryTextColor)
                4 -> this.animateColorChange(ThemeManager.theme.textViewTheme.quaternaryTextColor)
                5 -> this.animateColorChange(context.resolveAttrColor(R.attr.colorAppAccent))
            }
        } else {
            when (mode) {
                0 -> setTextColor(ThemeManager.theme.textViewTheme.headingTextColor)
                1 -> setTextColor(ThemeManager.theme.textViewTheme.primaryTextColor)
                2 -> setTextColor(ThemeManager.theme.textViewTheme.secondaryTextColor)
                3 -> setTextColor(ThemeManager.theme.textViewTheme.tertiaryTextColor)
                4 -> setTextColor(ThemeManager.theme.textViewTheme.quaternaryTextColor)
                5 -> setTextColor(context.resolveAttrColor(R.attr.colorAppAccent))
            }
        }
    }
}