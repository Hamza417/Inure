package app.simple.inure.decorations.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences.getAppFont
import app.simple.inure.util.TypeFace

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
        typeface = TypeFace.getTypeFace(getAppFont(), typedArray.getInt(R.styleable.TypeFaceTextView_appFontStyle, 0), context)
        isSelected = true
    }
}
