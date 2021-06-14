package app.simple.inure.decorations.behaviors

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import app.simple.inure.util.NullSafety.isNotNull

class OverScrollBehavior() : CoordinatorLayout.Behavior<View>() {

    private var springAnimation: SpringAnimation? = null

    companion object {
        private const val OVER_SCROLL_AREA = 1.5F
        private var overScrollY = 0F
    }

    constructor(context: Context, attributeSet: AttributeSet) : this() {
        unused(context, attributeSet)
    }

    /**
     * This is an useless method only here is to suppress
     * the lint warning of variable not used
     *
     * Removing them will cause the app to crash
     */
    private fun unused(context: Context, attributeSet: AttributeSet): Boolean {
        return context == attributeSet
    }

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: View,
            directTargetChild: View,
            target: View,
            axes: Int,
            type: Int,
    ): Boolean {
        overScrollY = 0F
        val group = target as ViewGroup
        val count = group.childCount
        if (springAnimation.isNotNull()) springAnimation!!.cancel()
        for (i in 0 until count) {
            val view = group.getChildAt(i)
            view.clearAnimation()
        }
        return true
    }

    override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: View,
            target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            type: Int,
            consumed: IntArray,
    ) {

        overScrollY -= dyUnconsumed / OVER_SCROLL_AREA
        val group = target as ViewGroup
        val count = group.childCount
        for (i in 0 until count) {
            val view = group.getChildAt(i)
            view.translationY = overScrollY
        }
    }

    override fun onStopNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: View,
            target: View,
            type: Int,
    ) {
        // Smooth animate to 0 when the user stops scrolling
        moveToDefPosition(target)
    }

    override fun onNestedPreFling(
            coordinatorLayout: CoordinatorLayout,
            child: View,
            target: View,
            velocityX: Float,
            velocityY: Float,
    ): Boolean {
        // Scroll view by inertia when current position equals to 0
        if (overScrollY == 0F) {
            return false
        }
        // Smooth animate to 0 when user fling view
        moveToDefPosition(target)
        return true
    }

    /**
     * [SpringForce.DAMPING_RATIO_NO_BOUNCY] will remove the bouncy effect, bouncy effect
     * is a short lived animation pleasure and not feasible for having to see a view
     * bouncing all the time
     *
     * [SpringForce.STIFFNESS_LOW] will give smooth over-scroll restoration effect
     */
    private fun moveToDefPosition(target: View) {
        val group = target as ViewGroup
        val count = group.childCount
        for (i in 0 until count) {
            val view = group.getChildAt(i)

            springAnimation = SpringAnimation(view, DynamicAnimation.TRANSLATION_Y)
            springAnimation!!.spring = SpringForce()
                    .setFinalPosition(0f)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_LOW)

            springAnimation!!.start()
        }
    }
}