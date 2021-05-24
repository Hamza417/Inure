package app.simple.inure.decorations.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.TypeFace

class TypeFaceEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.TypeFaceTextView, 0, 0)
        typeface = TypeFace.getTypeFace(AppearancePreferences.getAppFont(), typedArray.getInt(R.styleable.TypeFaceTextView_appFontStyle, 0), context)
        isSelected = true
    }
}