package app.simple.inure.decorations.layoutmanager.stack

import android.view.View

abstract class StackLayout(
        scrollOrientation: StackLayoutManager.ScrollOrientation,
        visibleCount: Int,
        perItemOffset: Int,
) {

    protected val mScrollOrientation = scrollOrientation
    protected var mVisibleCount = visibleCount
    protected var mPerItemOffset = perItemOffset

    internal fun setItemOffset(offset: Int) {
        mPerItemOffset = offset
    }

    internal fun getItemOffset(): Int {
        return mPerItemOffset
    }

    /**
     * External callback, used for layout.
     * @param firstMovePercent The percentage of movement of the first visible item, when firstMovePercent is infinitely close to 1.
     * @param itemView The current itemView.
     * @param position The position corresponding to the current itemView, position = 0 until visibleCount.
     */
    abstract fun doLayout(
            stackLayoutManager: StackLayoutManager,
            scrollOffset: Int,
            firstMovePercent: Float,
            itemView: View,
            position: Int,
    )

    abstract fun requestLayout()
}