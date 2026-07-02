package app.simple.inure.decorations.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

open class MaxHeightRecyclerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    // Custom property that requests a layout pass whenever updated
    var maxHeight: Int = -1
        set(value) {
            field = value
            requestLayout()
        }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var newHeightSpec = heightSpec
        // If a maxHeight is set, force the MeasureSpec to be AT_MOST that height
        if (maxHeight > 0) {
            newHeightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthSpec, newHeightSpec)
    }
}