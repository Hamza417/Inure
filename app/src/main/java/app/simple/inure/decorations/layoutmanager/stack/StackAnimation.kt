package app.simple.inure.decorations.layoutmanager.stack

import android.view.View
import app.simple.inure.decorations.layoutmanager.stack.StackLayoutManager.ScrollOrientation

abstract class StackAnimation(scrollOrientation: ScrollOrientation, visibleCount: Int) {

    protected val mScrollOrientation = scrollOrientation
    protected var mVisibleCount = visibleCount

    internal fun setVisibleCount(visibleCount: Int) {
        mVisibleCount = visibleCount
    }

    /**
     * External callback, used for animation.
     * @param firstMovePercent The percentage of movement of the first visible item, when firstMovePercent is infinitely close to 1.
     * @param itemView The current itemView.
     * @param position The position corresponding to the current itemView, position = 0 until visibleCount.
     */
    abstract fun doAnimation(firstMovePercent: Float, itemView: View, position: Int)
}