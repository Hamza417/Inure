package app.simple.inure.decorations.typeface

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Layout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences.getAppFont
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ColorUtils.animateColorChange
import app.simple.inure.util.ColorUtils.animateDrawableColorChange
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.TypeFace
import app.simple.inure.util.ViewUtils.fadInAnimation
import app.simple.inure.util.ViewUtils.fadOutAnimation

open class TypeFaceTextView : AppCompatTextView, ThemeChangedListener {

    private val typedArray: TypedArray
    private var colorMode: Int = 1
    private var drawableTintMode = 2
    private var isDrawableHidden = true
    private var lastDrawableColor = Color.GRAY

    constructor(context: Context) : super(context) {
        typedArray = context.theme.obtainStyledAttributes(null, R.styleable.TypeFaceTextView, 0, 0)
        init()
    }

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
        drawableTintMode = typedArray.getInt(R.styleable.TypeFaceTextView_drawableTintStyle, 3)
        isDrawableHidden = typedArray.getBoolean(R.styleable.TypeFaceTextView_isDrawableHidden, true)

        hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            breakStrategy = LineBreaker.BREAK_STRATEGY_SIMPLE
        }

        setTextColor(false)

        if (DevelopmentPreferences.isPreferencesIndicatorHidden() && isDrawableHidden) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        } else {
            setDrawableTint(false)
        }

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

    override fun onThemeChanged(theme: Theme, animate: Boolean) {
        setTextColor(animate = animate)
        setDrawableTint(animate = animate)
    }

    override fun setCompoundDrawablesWithIntrinsicBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
        setDrawableTint(false)
    }

    private fun setTextColor(animate: Boolean) {
        if (animate) {
            when (colorMode) {
                0 -> this.animateColorChange(ThemeManager.theme.textViewTheme.headingTextColor)
                1 -> this.animateColorChange(ThemeManager.theme.textViewTheme.primaryTextColor)
                2 -> this.animateColorChange(ThemeManager.theme.textViewTheme.secondaryTextColor)
                3 -> this.animateColorChange(ThemeManager.theme.textViewTheme.tertiaryTextColor)
                4 -> this.animateColorChange(ThemeManager.theme.textViewTheme.quaternaryTextColor)
                5 -> setTextColor(context.resolveAttrColor(R.attr.colorAppAccent)) // Accent Color won't change on theme change
                6 -> setTextColor(ColorStateList.valueOf(Color.WHITE))
            }
        } else {
            when (colorMode) {
                0 -> setTextColor(ThemeManager.theme.textViewTheme.headingTextColor)
                1 -> setTextColor(ThemeManager.theme.textViewTheme.primaryTextColor)
                2 -> setTextColor(ThemeManager.theme.textViewTheme.secondaryTextColor)
                3 -> setTextColor(ThemeManager.theme.textViewTheme.tertiaryTextColor)
                4 -> setTextColor(ThemeManager.theme.textViewTheme.quaternaryTextColor)
                5 -> setTextColor(context.resolveAttrColor(R.attr.colorAppAccent))
                6 -> setTextColor(ColorStateList.valueOf(Color.WHITE))
            }
        }
    }

    private fun setDrawableTint(animate: Boolean) {
        if (animate) {
            when (drawableTintMode) {
                // Accent Color won't change on theme change
                0 -> TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(context.resolveAttrColor(R.attr.colorAppAccent)))
                1 -> animateDrawableColorChange(lastDrawableColor, ThemeManager.theme.iconTheme.regularIconColor)
                2 -> animateDrawableColorChange(lastDrawableColor, ThemeManager.theme.iconTheme.secondaryIconColor)
            }
        } else {
            when (drawableTintMode) {
                0 -> TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(context.resolveAttrColor(R.attr.colorAppAccent)))
                1 -> TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(ThemeManager.theme.iconTheme.regularIconColor))
                2 -> TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(ThemeManager.theme.iconTheme.secondaryIconColor))
            }
        }

        setLastDrawableColor()
    }

    private fun setLastDrawableColor() {
        lastDrawableColor = when (drawableTintMode) {
            0 -> context.resolveAttrColor(R.attr.colorAppAccent)
            1 -> ThemeManager.theme.iconTheme.regularIconColor
            2 -> ThemeManager.theme.iconTheme.secondaryIconColor
            else -> ThemeManager.theme.iconTheme.secondaryIconColor
        }
    }

    fun setTextAnimation(text: String, duration: Long = 250, completion: (() -> Unit)? = null) {
        fadOutAnimation(duration) {
            this.text = text
            fadInAnimation(duration) {
                completion?.let {
                    it()
                }
            }
        }
    }

    fun setTextAnimation(resId: Int, duration: Long = 250, completion: (() -> Unit)? = null) {
        fadOutAnimation(duration) {
            this.text = context.getString(resId)
            fadInAnimation(duration) {
                completion?.let {
                    it()
                }
            }
        }
    }
}