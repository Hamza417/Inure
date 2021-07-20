package app.simple.inure.decorations.layoutmanager.stack

import android.view.View

class DefaultLayout(
        scrollOrientation: StackLayoutManager.ScrollOrientation,
        visibleCount: Int,
        perItemOffset: Int,
) : StackLayout(scrollOrientation, visibleCount, perItemOffset) {

    private var mHasMeasureItemSize = false
    private var mWidthSpace = 0
    private var mHeightSpace = 0
    private var mStartMargin = 0

    private var mWidth = 0
    private var mHeight = 0
    private var mScrollOffset = 0

    override fun doLayout(stackLayoutManager: StackLayoutManager, scrollOffset: Int, firstMovePercent: Float, itemView: View, position: Int) {
        mWidth = stackLayoutManager.width
        mHeight = stackLayoutManager.height
        mScrollOffset = scrollOffset
        if (!mHasMeasureItemSize) {
            mWidthSpace = mWidth - stackLayoutManager.getDecoratedMeasuredWidth(itemView)
            mHeightSpace = mHeight - stackLayoutManager.getDecoratedMeasuredHeight(itemView)
            mStartMargin = getStartMargin()
            mHasMeasureItemSize = true
        }
        val left: Int
        val top: Int
        if (position == 0) {
            left = getFirstVisibleItemLeft()
            top = getFirstVisibleItemTop()
        } else {
            left = getAfterFirstVisibleItemLeft(position, firstMovePercent)
            top = getAfterFirstVisibleItemTop(position, firstMovePercent)
        }

        val right = left + stackLayoutManager.getDecoratedMeasuredWidth(itemView)
        val bottom = top + stackLayoutManager.getDecoratedMeasuredHeight(itemView)

        stackLayoutManager.layoutDecorated(itemView, left, top, right, bottom)
    }

    override fun requestLayout() {
        mHasMeasureItemSize = false //表示尺寸可能发生了改变
    }

    private fun getFirstVisibleItemLeft(): Int {
        return when (mScrollOrientation) {
            StackLayoutManager.ScrollOrientation.RIGHT_TO_LEFT -> mStartMargin - mScrollOffset % mWidth
            StackLayoutManager.ScrollOrientation.LEFT_TO_RIGHT -> {
                return if (mScrollOffset % mWidth == 0) {
                    mStartMargin
                } else {
                    mStartMargin + (mWidth - mScrollOffset % mWidth)
                }
            }
            else -> mWidthSpace / 2
        }
    }

    private fun getFirstVisibleItemTop(): Int {
        return when (mScrollOrientation) {
            StackLayoutManager.ScrollOrientation.BOTTOM_TO_TOP -> mStartMargin - mScrollOffset % mHeight
            StackLayoutManager.ScrollOrientation.TOP_TO_BOTTOM -> {
                return if (mScrollOffset % mHeight == 0) {
                    mStartMargin
                } else {
                    mStartMargin + (mHeight - mScrollOffset % mHeight)
                }
            }
            else -> mHeightSpace / 2
        }
    }

    private fun getAfterFirstVisibleItemLeft(visiblePosition: Int, movePercent: Float): Int {
        return when (mScrollOrientation) {
            StackLayoutManager.ScrollOrientation.RIGHT_TO_LEFT -> (mStartMargin + mPerItemOffset * (visiblePosition - movePercent)).toInt()
            StackLayoutManager.ScrollOrientation.LEFT_TO_RIGHT -> (mStartMargin - mPerItemOffset * (visiblePosition - movePercent)).toInt()
            else -> mWidthSpace / 2
        }
    }

    private fun getAfterFirstVisibleItemTop(visiblePosition: Int, movePercent: Float): Int {
        return when (mScrollOrientation) {
            StackLayoutManager.ScrollOrientation.BOTTOM_TO_TOP -> (mStartMargin + mPerItemOffset * (visiblePosition - movePercent)).toInt()
            StackLayoutManager.ScrollOrientation.TOP_TO_BOTTOM -> (mStartMargin - mPerItemOffset * (visiblePosition - movePercent)).toInt()
            else -> mHeightSpace / 2
        }
    }

    private fun getStartMargin(): Int {
        return when (mScrollOrientation) {
            StackLayoutManager.ScrollOrientation.RIGHT_TO_LEFT, StackLayoutManager.ScrollOrientation.LEFT_TO_RIGHT -> mWidthSpace / 2
            else -> mHeightSpace / 2
        }
    }
}
