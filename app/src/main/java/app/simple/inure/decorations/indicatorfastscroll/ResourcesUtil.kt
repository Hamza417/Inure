package app.simple.inure.decorations.indicatorfastscroll

import android.content.res.ColorStateList
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes

internal fun View.throwIfMissingAttrs(@StyleRes styleRes: Int, block: () -> Unit) {
    try {
        block()
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException(
          "This ${this::class.java.simpleName} is missing an attribute. " +
                  "Add it to its style, or make the style inherit from " +
                  "${resources.getResourceName(styleRes)}.",
          e
        )
    }
}

@ColorInt
internal fun ColorStateList.getColorForState(stateSet: IntArray): Int? {
    return getColorForState(stateSet, defaultColor).takeIf { it != defaultColor }
}
