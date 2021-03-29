package app.simple.inure.decorations.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import app.simple.inure.R
import app.simple.inure.preferences.MainPreferences.getAppFont

open class TypeFaceTextView : AppCompatTextView {
    private val typedArray: TypedArray

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.TypeFaceTextView, 0, 0)
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.TypeFaceTextView, defStyleAttr, 0)
        init()
    }

    private fun init() {
        setTypeFace(getAppFont(), typedArray.getInt(R.styleable.TypeFaceTextView_appFontStyle, 0))
        isSelected = true
    }

    private fun setTypeFace(appFont: String, style: Int) {
        when (appFont) {
            LATO -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.lato_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.lato_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.lato_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.lato_bold)
                    }
                }
            }
            PLUS_JAKARTA -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_bold)
                    }
                }
            }
            MULISH -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mulish_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mulish_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mulish_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mulish_bold)
                    }
                }
            }
            JOST -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.jost_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.jost_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.jost_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.jost_bold)
                    }
                }
            }
            "roboto" -> {
            }
        }
    }

    companion object {
        const val PLUS_JAKARTA = "plus_jakarta"
        const val LATO = "lato"
        const val MULISH = "mulish"
        const val JOST = "jost"
    }
}