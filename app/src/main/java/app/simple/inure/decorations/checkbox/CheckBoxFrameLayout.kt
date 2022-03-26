package app.simple.inure.decorations.checkbox

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

open class CheckBoxFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    init {
        layoutParams = LayoutParams(context.resources.getDimensionPixelSize(R.dimen.button_size),
                                    context.resources.getDimensionPixelSize(R.dimen.button_size))

        minimumWidth = resources.getDimensionPixelOffset(R.dimen.checkbox_dimensions)
        minimumHeight = resources.getDimensionPixelOffset(R.dimen.checkbox_dimensions)

        backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)

        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, AppearancePreferences.getCornerRadius() / 4F)
            .build()

        background = MaterialShapeDrawable(shapeAppearanceModel)
    }
}