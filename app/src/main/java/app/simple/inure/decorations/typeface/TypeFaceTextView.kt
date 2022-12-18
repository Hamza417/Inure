package app.simple.inure.decorations.typeface

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Layout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.AppearancePreferences.getAppFont
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ColorUtils.animateColorChange
import app.simple.inure.util.ColorUtils.animateDrawableColorChange
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.TypeFace
import app.simple.inure.util.ViewUtils.fadInAnimation
import app.simple.inure.util.ViewUtils.fadOutAnimation

open class TypeFaceTextView : AppCompatTextView, ThemeChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val typedArray: TypedArray
    private var colorMode: Int = 1
    private var drawableTintMode = 2
    private var isDrawableHidden = true
    private var lastDrawableColor = Color.GRAY

    var fontStyle = MEDIUM
        set(value) {
            field = value
            typeface = TypeFace.getTypeFace(getAppFont(), field, context)
        }

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
        if (isInEditMode) return
        typeface = TypeFace.getTypeFace(getAppFont(), typedArray.getInt(R.styleable.TypeFaceTextView_appFontStyle, BOLD), context)
        colorMode = typedArray.getInt(R.styleable.TypeFaceTextView_textColorStyle, 1)
        drawableTintMode = typedArray.getInt(R.styleable.TypeFaceTextView_drawableTintStyle, 3)
        isDrawableHidden = typedArray.getBoolean(R.styleable.TypeFaceTextView_isDrawableHidden, true)

        hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            breakStrategy = LineBreaker.BREAK_STRATEGY_SIMPLE
        }

        setTextColor(false)

        if (DevelopmentPreferences.get(DevelopmentPreferences.preferencesIndicator) && isDrawableHidden) {
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
        if (isInEditMode.invert()) {
            app.simple.inure.preferences.SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
        }
        ThemeManager.addListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        app.simple.inure.preferences.SharedPreferences.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
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
                5 -> this.animateColorChange(AppearancePreferences.getAccentColor())
                6 -> setTextColor(ColorStateList.valueOf(Color.WHITE))
            }
        } else {
            when (colorMode) {
                0 -> setTextColor(ThemeManager.theme.textViewTheme.headingTextColor)
                1 -> setTextColor(ThemeManager.theme.textViewTheme.primaryTextColor)
                2 -> setTextColor(ThemeManager.theme.textViewTheme.secondaryTextColor)
                3 -> setTextColor(ThemeManager.theme.textViewTheme.tertiaryTextColor)
                4 -> setTextColor(ThemeManager.theme.textViewTheme.quaternaryTextColor)
                5 -> setTextColor(AppearancePreferences.getAccentColor())
                6 -> setTextColor(ColorStateList.valueOf(Color.WHITE))
            }
        }
    }

    private fun setDrawableTint(animate: Boolean) {
        if (animate) {
            when (drawableTintMode) {
                0 -> animateDrawableColorChange(lastDrawableColor, AppearancePreferences.getAccentColor())
                1 -> animateDrawableColorChange(lastDrawableColor, ThemeManager.theme.iconTheme.regularIconColor)
                2 -> animateDrawableColorChange(lastDrawableColor, ThemeManager.theme.iconTheme.secondaryIconColor)
            }
        } else {
            when (drawableTintMode) {
                0 -> TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(AppearancePreferences.getAccentColor()))
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

    fun setStrikeThru(boolean: Boolean) {
        paintFlags = if (boolean) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_tiny, 0)
            paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.accentColor -> {
                setTextColor(animate = true)
                setDrawableTint(animate = true)
            }
        }
    }

    companion object {
        const val LIGHT = 0
        const val REGULAR = 1
        const val MEDIUM = 2
        const val BOLD = 3
    }
}