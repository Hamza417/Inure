package app.simple.inure.decorations.ime

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup

object Utils {
    private val tmpIntArr = IntArray(2)

    /**
     * Function which updates the given [rect] with this view's position and bounds in its window.
     */
    fun View.copyBoundsInWindow(rect: Rect) {
        if (isLaidOut && isAttachedToWindow) {
            rect.set(0, 0, width, height)
            getLocationInWindow(tmpIntArr)
            rect.offset(tmpIntArr[0], tmpIntArr[1])
        } else {
            throw IllegalArgumentException(
                    "Can not copy bounds as view is not laid out" +
                            " or attached to window"
            )
        }
    }

    /**
     * Provides access to the hidden ViewGroup#suppressLayout method.
     */
    fun ViewGroup.suppressLayoutCompat(suppress: Boolean) {
        if (Build.VERSION.SDK_INT >= 29) {
            suppressLayout(suppress)
        } else {
            hiddenSuppressLayout(this, suppress)
        }
    }

    /**
     * False when linking of the hidden suppressLayout method has previously failed.
     */
    private var tryHiddenSuppressLayout = true

    @SuppressLint("NewApi") // Lint doesn't know about the hidden method.
    private fun hiddenSuppressLayout(group: ViewGroup, suppress: Boolean) {
        if (tryHiddenSuppressLayout) {
            // Since this was an @hide method made public, we can link directly against it with
            // a try/catch for its absence instead of doing the same through reflection.
            try {
                group.suppressLayout(suppress)
            } catch (e: NoSuchMethodError) {
                tryHiddenSuppressLayout = false
            }
        }
    }
}