package app.simple.inure.decorations.typeface

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
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
import app.simple.inure.util.ColorUtils
import app.simple.inure.util.ColorUtils.animateColorChange
import app.simple.inure.util.ColorUtils.animateDrawableColorChange
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.TypeFace
import app.simple.inure.util.ViewUtils
import app.simple.inure.util.ViewUtils.fadeInAnimation
import app.simple.inure.util.ViewUtils.fadeOutAnimation
import app.simple.inure.util.ViewUtils.slideInAnimation
import app.simple.inure.util.ViewUtils.slideOutAnimation

@Suppress("unused")
open class TypeFaceTextView : AppCompatTextView, ThemeChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val typedArray: TypedArray
    private var colorMode: Int = 1
    private var drawableTintMode = ICON_TINT_REGULAR
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
        drawableTintMode = typedArray.getInt(R.styleable.TypeFaceTextView_drawableTintStyle, 1)
        isDrawableHidden = typedArray.getBoolean(R.styleable.TypeFaceTextView_isDrawableHidden, true)

        hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            breakStrategy = LineBreaker.BREAK_STRATEGY_SIMPLE
        }

        setTextColor(false)

        if (DevelopmentPreferences.get(DevelopmentPreferences.TEXTVIEW_DRAWABLE_INDICATOR) && isDrawableHidden) {
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

    override fun setCompoundDrawablesWithIntrinsicBounds(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
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
                7 -> setTextColor(ColorStateList.valueOf(Color.BLACK))
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
                7 -> setTextColor(ColorStateList.valueOf(Color.BLACK))
            }
        }
    }

    private fun setDrawableTint(animate: Boolean) {
        if (animate) {
            when (drawableTintMode) {
                ICON_TINT_ACCENT, ICON_TINT_WARNING, ICON_TINT_SUCCESS -> {
                    animateDrawableColorChange(lastDrawableColor, AppearancePreferences.getAccentColor())
                }
                ICON_TINT_REGULAR -> {
                    animateDrawableColorChange(lastDrawableColor, ThemeManager.theme.iconTheme.regularIconColor)
                }
                ICON_TINT_SECONDARY -> {
                    animateDrawableColorChange(lastDrawableColor, ThemeManager.theme.iconTheme.secondaryIconColor)
                }
                ICON_TINT_ERROR -> {
                    animateDrawableColorChange(lastDrawableColor, Color.RED)
                }
                ICON_TINT_CUSTOM -> {
                    /* no-op */
                }
            }
        } else {
            when (drawableTintMode) {
                ICON_TINT_ACCENT, ICON_TINT_WARNING, ICON_TINT_SUCCESS -> {
                    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(AppearancePreferences.getAccentColor()))
                }
                ICON_TINT_REGULAR -> {
                    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(ThemeManager.theme.iconTheme.regularIconColor))
                }
                ICON_TINT_SECONDARY -> {
                    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(ThemeManager.theme.iconTheme.secondaryIconColor))
                }
                ICON_TINT_ERROR -> {
                    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(Color.RED))
                }
                ICON_TINT_CUSTOM -> {
                    /* no-op */
                }
            }
        }

        if (drawableTintMode != ICON_TINT_CUSTOM) {
            setLastDrawableColor()
        }
    }

    private fun extracted() {
        TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(Color.GRAY))
    }

    protected fun getDrawableTintColor(): Int {
        return when (drawableTintMode) {
            ICON_TINT_ACCENT, ICON_TINT_WARNING, ICON_TINT_SUCCESS -> AppearancePreferences.getAccentColor()
            ICON_TINT_REGULAR -> ThemeManager.theme.iconTheme.regularIconColor
            ICON_TINT_SECONDARY -> ThemeManager.theme.iconTheme.secondaryIconColor
            ICON_TINT_ERROR -> Color.RED // Error
            else -> ThemeManager.theme.iconTheme.secondaryIconColor
        }
    }

    private fun setLastDrawableColor() {
        lastDrawableColor = when (drawableTintMode) {
            ICON_TINT_ACCENT -> context.resolveAttrColor(R.attr.colorAppAccent)
            ICON_TINT_REGULAR -> ThemeManager.theme.iconTheme.regularIconColor
            ICON_TINT_SECONDARY -> ThemeManager.theme.iconTheme.secondaryIconColor
            else -> ThemeManager.theme.iconTheme.secondaryIconColor
        }
    }

    fun setRightDrawable(resId: Int) {
        setCompoundDrawablesWithIntrinsicBounds(0, 0, resId, 0)
    }

    fun setRightDrawable(drawable: Drawable) {
        setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }

    fun setStrikeThru(boolean: Boolean) {
        paintFlags = if (boolean) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_12dp, 0)
            paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
    }

    fun setTrackingIcon(isTracker: Boolean) {
        if (isTracker) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_radiation_nuclear_12dp, 0)
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        drawableTintMode = ICON_TINT_WARNING
        setDrawableTint(false)
    }

    fun setHiddenIcon(isHidden: Boolean) {
        @Suppress("LiftReturnOrAssignment")
        if (isHidden) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off_12dp, 0)
            alpha = 0.75f
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            alpha = 1f
        }

        drawableTintMode = ICON_TINT_ACCENT
        setDrawableTint(false)
    }

    fun setDrawableTintMode(mode: Int, animate: Boolean = false) {
        drawableTintMode = mode
        setDrawableTint(animate)
    }

    fun setFOSSIcon(isFOSS: Boolean) {
        if (isFOSS) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_open_source_12dp, 0)
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        drawableTintMode = ICON_TINT_ACCENT
        setDrawableTint(false)
    }

    fun setBloatIcon(isBloat: Boolean) {
        if (isBloat) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_recycling_12dp, 0)
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        drawableTintMode = ICON_TINT_ACCENT
        setDrawableTint(false)
    }

    fun setWarningIcon(isWarning: Boolean, tintMode: Int = ICON_TINT_SECONDARY) {
        if (isWarning) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_warning_12dp, 0)
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        drawableTintMode = tintMode
        setDrawableTint(false)
    }

    fun setDangerousPermissionIcon(isDangerous: Boolean) {
        if (isDangerous) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_skull_12dp, 0)
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        drawableTintMode = ICON_TINT_ACCENT
        setDrawableTint(false)
    }

    fun setTextWithAnimation(text: String, duration: Long = 250, completion: (() -> Unit)? = null) {
        fadeOutAnimation(duration) {
            this.text = text
            fadeInAnimation(duration) {
                completion?.let {
                    it()
                }
            }
        }
    }

    fun setTextWithSlideAnimation(text: String, duration: Long = 250, direction: Int = ViewUtils.LEFT, delay: Long = 0L, completion: (() -> Unit)? = null) {
        slideOutAnimation(duration, delay / 2L, direction) {
            this.text = text
            slideInAnimation(duration, delay / 2L, direction) {
                completion?.let {
                    it()
                }
            }
        }
    }

    fun setTextWithAnimation(resId: Int, duration: Long = 250, completion: (() -> Unit)? = null) {
        fadeOutAnimation(duration) {
            this.text = context.getString(resId)
            fadeInAnimation(duration) {
                completion?.let {
                    it()
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.ACCENT_COLOR -> {
                setTextColor(animate = true)
                setDrawableTint(animate = true)
            }
        }
    }

    fun enableSelection() {
        if (DevelopmentPreferences.get(DevelopmentPreferences.IS_TEXT_SELECTABLE)) {
            setTextIsSelectable(true)
            setHighlightColor()
        }
    }

    private fun setHighlightColor() {
        highlightColor = ColorUtils.lightenColor(AppearancePreferences.getAccentColor(), 0.2F)
    }

    companion object {
        const val LIGHT = 0
        const val REGULAR = 1
        const val MEDIUM = 2
        const val BOLD = 3

        const val ICON_TINT_ACCENT = 0
        const val ICON_TINT_REGULAR = 1
        const val ICON_TINT_SECONDARY = 2
        const val ICON_TINT_WARNING = 3
        const val ICON_TINT_ERROR = 4
        const val ICON_TINT_SUCCESS = 5
        const val ICON_TINT_CUSTOM = 6
    }
}
