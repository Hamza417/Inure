package app.simple.inure.decorations.bouncescroll

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.OverScroller
import android.widget.ScrollView
import androidx.annotation.RestrictTo
import androidx.core.view.*
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityRecordCompat
import androidx.core.widget.EdgeEffectCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory
import app.simple.inure.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Modified based on NestedScrollView source code
 */

@Suppress("MemberVisibilityCanBePrivate", "unused")
class BouncyNestedScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr),
      NestedScrollingParent3,
      NestedScrollingChild3,
      ScrollingView {

    interface OnScrollChangeListener {
        fun onScrollChange(v: BouncyNestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int)
    }

    private var mLastScroll: Long = 0
    private val mTempRect = Rect()
    private var mScroller: OverScroller? = null
    private var mEdgeGlowTop: BouncyEdgeEffect? = null
    private var mEdgeGlowBottom: BouncyEdgeEffect? = null

    /**
     * Position of the last motion event.
     */
    private var mLastMotionY = 0

    /**
     * True when the layout has changed but the traversal has not come through yet.
     * Ideally the view hierarchy would keep track of this for us.
     */
    private var mIsLayoutDirty = true
    private var mIsLaidOut = false

    /**
     * The child to give focus to in the event that a child has requested focus while the
     * layout is dirty. This prevents the scroll from being wrong if the child has not been
     * laid out before requesting focus.
     */
    private var mChildToScrollTo: View? = null

    /**
     * True if the user is currently dragging this ScrollView around. This is
     * not the same as 'is being flung', which can be checked by
     * mScroller.isFinished() (flinging begins when the user lifts his finger).
     */
    private var mIsBeingDragged = false

    /**
     * Determines speed during touch scrolling
     */
    private var mVelocityTracker: VelocityTracker? = null

    /**
     * When set to true, the scroll view measure its child to make it fill the currently
     * visible area.
     */
    private var mFillViewport = false

    /**
     * Whether arrow scrolling is animated.
     */
    var isSmoothScrollingEnabled = true
    private var mTouchSlop = 0
    private var mMinimumVelocity = 0
    private var mMaximumVelocity = 0


    //bouncy attributes
    var overscrollAnimationSize = 0.5f
    var flingAnimationSize = 0.5f

    var dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        set(value) {
            field = value
            this.spring.spring = SpringForce()
                    .setFinalPosition(0f)
                    .setDampingRatio(value)
                    .setStiffness(stiffness)
        }

    var stiffness = SpringForce.STIFFNESS_LOW
        set(value) {
            field = value
            this.spring.spring = SpringForce()
                    .setFinalPosition(0f)
                    .setDampingRatio(dampingRatio)
                    .setStiffness(value)
        }

    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private var mActivePointerId = INVALID_POINTER

    /**
     * Used during scrolling to retrieve the new offset within the window.
     */
    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)
    private var mNestedYOffset = 0
    private var mSavedState: SavedState? = null
    private val mParentHelper: NestedScrollingParentHelper
    private val mChildHelper: NestedScrollingChildHelper
    private var mVerticalScrollFactor = 0f
    var mOnScrollChangeListener: OnScrollChangeListener? = null

    private val spring = SpringAnimation(this, SpringAnimation.TRANSLATION_Y)
            .setSpring(
                SpringForce()
                        .setFinalPosition(0f)
                        .setDampingRatio(dampingRatio)
                        .setStiffness(stiffness)
            )


    // NestedScrollingChild3
    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int, consumed: IntArray) =
        mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed)


    // NestedScrollingChild2
    override fun startNestedScroll(axes: Int, type: Int): Boolean =
        mChildHelper.startNestedScroll(axes, type)


    override fun stopNestedScroll(type: Int) = mChildHelper.stopNestedScroll(type)


    override fun hasNestedScrollingParent(type: Int): Boolean =
        mChildHelper.hasNestedScrollingParent(type)


    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int): Boolean =
        mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type)

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?, type: Int): Boolean =
        mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)


    // NestedScrollingChild
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean = mChildHelper.isNestedScrollingEnabled

    override fun startNestedScroll(axes: Int): Boolean =
        startNestedScroll(axes, ViewCompat.TYPE_TOUCH)

    override fun stopNestedScroll() = stopNestedScroll(ViewCompat.TYPE_TOUCH)

    override fun hasNestedScrollingParent(): Boolean =
        hasNestedScrollingParent(ViewCompat.TYPE_TOUCH)

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean =
        mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean =
        dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, ViewCompat.TYPE_TOUCH)

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean =
        mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean =
        mChildHelper.dispatchNestedPreFling(velocityX, velocityY)

    // NestedScrollingParent3
    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) =
        onNestedScrollInternal(dyUnconsumed, type, consumed)

    private fun onNestedScrollInternal(dyUnconsumed: Int, type: Int, consumed: IntArray?) {
        val oldScrollY = scrollY
        scrollBy(0, dyUnconsumed)
        val myConsumed = scrollY - oldScrollY
        if (consumed != null) {
            consumed[1] += myConsumed
        }
        val myUnconsumed = dyUnconsumed - myConsumed
        mChildHelper.dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null, type, consumed)
    }

    // NestedScrollingParent2
    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean =
        axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type)
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        mParentHelper.onStopNestedScroll(target, type)
        stopNestedScroll(type)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) =
        onNestedScrollInternal(dyUnconsumed, type, null)

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        dispatchNestedPreScroll(dx, dy, consumed, null, type)
    }

    // NestedScrollingParent
    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean =
        onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) =
        onNestedScrollAccepted(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)

    override fun onStopNestedScroll(target: View) =
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH)

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) =
        onNestedScrollInternal(dyUnconsumed, ViewCompat.TYPE_TOUCH, null)

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) =
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH)

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return if (!consumed) {
            dispatchNestedFling(0f, velocityY, true)
            fling(velocityY.toInt())
            true
        } else false
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean =
        dispatchNestedPreFling(velocityX, velocityY)

    override fun getNestedScrollAxes(): Int = mParentHelper.nestedScrollAxes

    // ScrollView import
    override fun shouldDelayChildPressedState(): Boolean = true

    override fun getTopFadingEdgeStrength(): Float {
        if (childCount == 0) return 0.0f

        val length = verticalFadingEdgeLength
        val scrollY = scrollY
        return if (scrollY < length)
            scrollY / length.toFloat()
        else 1.0f
    }

    override fun getBottomFadingEdgeStrength(): Float {
        if (childCount == 0) return 0.0f

        val child = getChildAt(0)
        val lp = child.layoutParams as LayoutParams
        val length = verticalFadingEdgeLength
        val bottomEdge = height - paddingBottom
        val span = child.bottom + lp.bottomMargin - scrollY - bottomEdge
        return if (span < length)
            span / length.toFloat()
        else 1.0f
    }

    /**
     * @return The maximum amount this scroll view will scroll in response to
     * an arrow event.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val maxScrollAmount: Int
        get() = (MAX_SCROLL_FACTOR * height).toInt()

    private fun initScrollView() {
        mScroller = OverScroller(context)
        isFocusable = true
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
        setWillNotDraw(false)
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
    }

    override fun addView(child: View) {
        check(childCount <= 0) { "ScrollView can host only one direct child" }
        super.addView(child)
    }

    override fun addView(child: View, index: Int) {
        check(childCount <= 0) { "ScrollView can host only one direct child" }
        super.addView(child, index)
    }

    override fun addView(child: View, params: ViewGroup.LayoutParams) {
        check(childCount <= 0) { "ScrollView can host only one direct child" }
        super.addView(child, params)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        check(childCount <= 0) { "ScrollView can host only one direct child" }
        super.addView(child, index, params)
    }


    /**
     * @return Returns true this ScrollView can be scrolled
     */
    private fun canScroll(): Boolean {
        return if (childCount > 0) {
            val child = getChildAt(0)
            val lp = child.layoutParams as LayoutParams
            val childSize = child.height + lp.topMargin + lp.bottomMargin
            val parentSpace = height - paddingTop - paddingBottom
            childSize > parentSpace
        } else false
    }

    var isFillViewport: Boolean
        get() = mFillViewport
        set(fillViewport) {
            @Suppress("unused")
            if (fillViewport != mFillViewport) {
                mFillViewport = fillViewport
                requestLayout()
            }
        }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        mOnScrollChangeListener?.onScrollChange(this, l, t, oldl, oldt)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mFillViewport) {
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            if (heightMode != MeasureSpec.UNSPECIFIED) {
                if (childCount > 0) {
                    val child = getChildAt(0)
                    val lp = child.layoutParams as LayoutParams
                    val childSize = child.measuredHeight
                    val parentSpace = (measuredHeight - paddingTop - paddingBottom - lp.topMargin - lp.bottomMargin)
                    if (childSize < parentSpace) {
                        val childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin, lp.width)
                        val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(parentSpace, MeasureSpec.EXACTLY)
                        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                    }
                }
            }
        }
    }

    // Let the focused view and/or our descendants get the key first
    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        super.dispatchKeyEvent(event) || executeKeyEvent(event)

    /**
     * You can call this function yourself to have the scroll view perform
     * scrolling from a key event, just as if the event had been dispatched to
     * it by the view hierarchy.
     *
     * @param event The key event to execute.
     * @return Return true if the event was handled, else false.
     */
    fun executeKeyEvent(event: KeyEvent): Boolean {
        mTempRect.setEmpty()
        if (!canScroll()) {
            return if (isFocused && event.keyCode != KeyEvent.KEYCODE_BACK) {
                var currentFocused = findFocus()
                if (currentFocused === this)
                    currentFocused = null
                val nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, FOCUS_DOWN)
                nextFocused != null && nextFocused !== this && nextFocused.requestFocus(FOCUS_DOWN)
            } else false
        }
        var handled = false
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> handled =
                    if (!event.isAltPressed)
                        arrowScroll(FOCUS_UP)
                    else
                        fullScroll(FOCUS_UP)

                KeyEvent.KEYCODE_DPAD_DOWN -> handled =
                    if (!event.isAltPressed)
                        arrowScroll(FOCUS_DOWN)
                    else
                        fullScroll(FOCUS_DOWN)

                KeyEvent.KEYCODE_SPACE -> pageScroll(if (event.isShiftPressed) FOCUS_UP else FOCUS_DOWN)
            }
        }
        return handled
    }

    private fun inChild(x: Int, y: Int): Boolean {
        if (childCount > 0) {
            val scrollY = scrollY
            val child = getChildAt(0)
            return !(y < child.top - scrollY || y >= child.bottom - scrollY || x < child.left || x >= child.right)
        }
        return false
    }

    private fun initOrResetVelocityTracker() {
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain()
        else
            mVelocityTracker!!.clear()

    }

    private fun initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain()
    }

    private fun recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept)
            recycleVelocityTracker()
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */
        val action = ev.action

        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged)
            return true

        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> {

                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionY is set to the y value
                 * of the down event.
                 */
                val activePointerId = mActivePointerId
                if (activePointerId != INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    val pointerIndex = ev.findPointerIndex(activePointerId)

                    if (pointerIndex == -1)
                        Log.e(TAG, "Invalid pointerId=$activePointerId in onInterceptTouchEvent")

                    val y = ev.getY(pointerIndex).toInt()
                    val yDiff = abs(y - mLastMotionY)
                    if (yDiff > mTouchSlop && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL == 0) {
                        mIsBeingDragged = true
                        mLastMotionY = y
                        initVelocityTrackerIfNotExists()
                        mVelocityTracker!!.addMovement(ev)
                        mNestedYOffset = 0
                        val parent = parent
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val y = ev.y.toInt()
                if (!inChild(ev.x.toInt(), y)) {
                    mIsBeingDragged = false
                    recycleVelocityTracker()
                }

                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */

                mLastMotionY = y
                mActivePointerId = ev.getPointerId(0)
                initOrResetVelocityTracker()
                mVelocityTracker!!.addMovement(ev)

                /*
                 * If being flung
                 *  and user touches the screen, initiate drag;
                 * otherwise don't. mScroller.isFinished should be false when
                 * being flung
                 * . We need to call computeScrollOffset() first so that
                 * isFinished() is correct.
                 */

                mScroller!!.computeScrollOffset()
                mIsBeingDragged = !mScroller!!.isFinished
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                /* Release the drag */mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
                recycleVelocityTracker()
                if (mScroller!!.springBack(scrollX, scrollY, 0, 0, 0, scrollRange))
                    ViewCompat.postInvalidateOnAnimation(this)

                stopNestedScroll(ViewCompat.TYPE_TOUCH)
            }

            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */

        return mIsBeingDragged
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        initVelocityTrackerIfNotExists()
        val actionMasked = ev.actionMasked

        if (actionMasked == MotionEvent.ACTION_DOWN)
            mNestedYOffset = 0

        val vMotionEvent = MotionEvent.obtain(ev)
        vMotionEvent.offsetLocation(0f, mNestedYOffset.toFloat())
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (childCount == 0)
                    return false

                if (!mScroller!!.isFinished.also { mIsBeingDragged = it }) {
                    val parent = parent
                    parent?.requestDisallowInterceptTouchEvent(true)
                }

                /*
                 * If being flung
                 *  and user touches, stop the fling. isFinished
                 * will be false if being flung
                 * .
                 */

                if (!mScroller!!.isFinished)
                    abortAnimatedScroll()


                // Remember where the motion event started
                mLastMotionY = ev.y.toInt()
                mActivePointerId = ev.getPointerId(0)
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
            }

            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = ev.findPointerIndex(mActivePointerId)

                if (activePointerIndex == -1)
                    Log.e(TAG, "Invalid pointerId=$mActivePointerId in onTouchEvent")

                val y = ev.getY(activePointerIndex).toInt()
                var deltaY = mLastMotionY - y
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_TOUCH)) {
                    deltaY -= mScrollConsumed[1]
                    mNestedYOffset += mScrollOffset[1]
                }
                if (!mIsBeingDragged && abs(deltaY) > mTouchSlop) {
                    val parent = parent
                    parent?.requestDisallowInterceptTouchEvent(true)
                    mIsBeingDragged = true

                    if (deltaY > 0)
                        deltaY -= mTouchSlop
                    else
                        deltaY += mTouchSlop
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionY = y - mScrollOffset[1]
                    val oldY = scrollY
                    val range = scrollRange
                    val overscrollMode = overScrollMode
                    val canOverscroll = (overscrollMode == OVER_SCROLL_ALWAYS || overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0)

                    // Calling overScrollByCompat will call onOverScrolled, which
                    // calls onScrollChanged if applicable.

                    // Break our velocity if we hit a scroll barrier.
                    if (overScrollByCompat(deltaY, 0, scrollY, range, 0, 0, true) && !hasNestedScrollingParent(ViewCompat.TYPE_TOUCH))
                        mVelocityTracker!!.clear()

                    val scrolledDeltaY = scrollY - oldY
                    val unconsumedY = deltaY - scrolledDeltaY
                    mScrollConsumed[1] = 0
                    dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, mScrollOffset, ViewCompat.TYPE_TOUCH, mScrollConsumed)

                    mLastMotionY -= mScrollOffset[1]
                    mNestedYOffset += mScrollOffset[1]
                    if (canOverscroll) {
                        deltaY -= mScrollConsumed[1]
                        ensureGlows()
                        val pulledToY = oldY + deltaY
                        if (pulledToY < 0) {
                            EdgeEffectCompat.onPull(mEdgeGlowBottom!!, deltaY.toFloat() / height, ev.getX(activePointerIndex) / width)

                            if (!mEdgeGlowBottom!!.isFinished)
                                mEdgeGlowBottom!!.onRelease()

                        } else if (pulledToY > range) {
                            EdgeEffectCompat.onPull(mEdgeGlowBottom!!, deltaY.toFloat() / height, 1f - ev.getX(activePointerIndex) / width)

                            if (!mEdgeGlowTop!!.isFinished)
                                mEdgeGlowTop!!.onRelease()

                        }


                        if (mEdgeGlowTop != null && (!mEdgeGlowTop!!.isFinished || !mEdgeGlowBottom!!.isFinished))
                            ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val initialVelocity = velocityTracker.getYVelocity(mActivePointerId).toInt()
                if (abs(initialVelocity) > mMinimumVelocity) {
                    if (!dispatchNestedPreFling(0f, -initialVelocity.toFloat())) {
                        dispatchNestedFling(0f, -initialVelocity.toFloat(), true)
                        fling(-initialVelocity)
                    }
                } else if (mScroller!!.springBack(scrollX, scrollY, 0, 0, 0, scrollRange))
                    ViewCompat.postInvalidateOnAnimation(this)

                mActivePointerId = INVALID_POINTER
                endDrag()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mIsBeingDragged && childCount > 0 && mScroller!!.springBack(scrollX, scrollY, 0, 0, 0, scrollRange))
                    ViewCompat.postInvalidateOnAnimation(this)

                mActivePointerId = INVALID_POINTER
                endDrag()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                mLastMotionY = ev.getY(index).toInt()
                mActivePointerId = ev.getPointerId(index)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId)).toInt()
            }
        }


        mVelocityTracker?.addMovement(vMotionEvent)
        vMotionEvent.recycle()
        return true
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mLastMotionY = ev.getY(newPointerIndex).toInt()
            mActivePointerId = ev.getPointerId(newPointerIndex)
            mVelocityTracker?.clear()
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.source and InputDeviceCompat.SOURCE_CLASS_POINTER != 0) {
            if (event.action == MotionEvent.ACTION_SCROLL) {
                if (!mIsBeingDragged) {
                    val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                    if (vScroll != 0f) {
                        val delta = (vScroll * verticalScrollFactorCompat).toInt()
                        val range = scrollRange
                        val oldScrollY = scrollY
                        var newScrollY = oldScrollY - delta
                        if (newScrollY < 0)
                            newScrollY = 0
                        else if (newScrollY > range)
                            newScrollY = range
                        if (newScrollY != oldScrollY) {
                            super.scrollTo(scrollX, newScrollY)
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private val verticalScrollFactorCompat: Float
        get() {
            if (mVerticalScrollFactor == 0f) {
                val outValue = TypedValue()
                val context = context
                if (!context.theme.resolveAttribute(android.R.attr.listPreferredItemHeight, outValue, true))
                    throw IllegalStateException("Expected theme to define listPreferredItemHeight.")

                mVerticalScrollFactor = outValue.getDimension(context.resources.displayMetrics)
            }
            return mVerticalScrollFactor
        }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) =
        super.scrollTo(scrollX, scrollY)

    @Suppress("MemberVisibilityCanBePrivate", "UNUSED_PARAMETER")
    fun overScrollByCompat(deltaY: Int, scrollX: Int, scrollY: Int, scrollRangeY: Int, mX: Int, mY: Int, isTouchEvent: Boolean): Boolean {
        var maxOverScrollX = mX
        var maxOverScrollY = mY
        val overScrollMode = overScrollMode
        val canScrollHorizontal = computeHorizontalScrollRange() > computeHorizontalScrollExtent()
        val canScrollVertical = computeVerticalScrollRange() > computeVerticalScrollExtent()
        val overScrollHorizontal = (overScrollMode == OVER_SCROLL_ALWAYS || overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal)
        val overScrollVertical = (overScrollMode == OVER_SCROLL_ALWAYS || overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical)
        var newScrollX = scrollX

        if (!overScrollHorizontal)
            maxOverScrollX = 0

        var newScrollY = scrollY + deltaY

        if (!overScrollVertical)
            maxOverScrollY = 0


        // Clamp values if at the limits and record
        val left = -maxOverScrollX
        val right = maxOverScrollX
        val top = -maxOverScrollY
        val bottom = maxOverScrollY + scrollRangeY
        var clampedX = false
        if (newScrollX > right) {
            newScrollX = right
            clampedX = true
        } else if (newScrollX < left) {
            newScrollX = left
            clampedX = true
        }
        var clampedY = false
        if (newScrollY > bottom) {
            newScrollY = bottom
            clampedY = true
        } else if (newScrollY < top) {
            newScrollY = top
            clampedY = true
        }
        if (clampedY && !hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH) && !mIsBeingDragged)
            mScroller!!.springBack(newScrollX, newScrollY, 0, 0, 0, scrollRange)

        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY)
        return clampedX || clampedY
    }

    val scrollRange: Int
        get() {
            var scrollRange = 0
            if (childCount > 0) {
                val child = getChildAt(0)
                val lp = child.layoutParams as LayoutParams
                val childSize = child.height + lp.topMargin + lp.bottomMargin
                val parentSpace = height - paddingTop - paddingBottom
                scrollRange = max(0, childSize - parentSpace)
            }
            return scrollRange
        }

    /**
     *
     *
     * Finds the next focusable component that fits in the specified bounds.
     *
     *
     * @param topFocus look for a candidate is the one at the top of the bounds
     * if topFocus is true, or at the bottom of the bounds if topFocus is
     * false
     * @param top      the top offset of the bounds in which a focusable must be
     * found
     * @param bottom   the bottom offset of the bounds in which a focusable must
     * be found
     * @return the next focusable component in the bounds or null if none can
     * be found
     */
    private fun findFocusableViewInBounds(topFocus: Boolean, top: Int, bottom: Int): View? {
        val focusable: List<View> = getFocusables(FOCUS_FORWARD)
        var focusCandidate: View? = null

        /*
         * A fully contained focusable is one where its top is below the bound's
         * top, and its bottom is above the bound's bottom. A partially
         * contained focusable is one where some part of it is within the
         * bounds, but it also has some part that is not within bounds.  A fully contained
         * focusable is preferred to a partially contained focusable.
         */
        var foundFullyContainedFocusable = false
        for (view: View in focusable) {
            val viewTop = view.top
            val viewBottom = view.bottom
            if (top < viewBottom && viewTop < bottom) {
                /*
                 * the focusable is in the target area, it is a candidate for
                 * focusing
                 */
                val viewIsFullyContained = top < viewTop && viewBottom < bottom
                if (focusCandidate == null) {
                    /* No candidate, take this one */
                    focusCandidate = view
                    foundFullyContainedFocusable = viewIsFullyContained
                } else {
                    val viewIsCloserToBoundary = (topFocus && viewTop < focusCandidate.top || !topFocus && viewBottom > focusCandidate.bottom)
                    if (foundFullyContainedFocusable) {
                        if (viewIsFullyContained && viewIsCloserToBoundary)
                            focusCandidate = view
                    } else {
                        if (viewIsFullyContained) {
                            /* Any fully contained view beats a partially contained view */
                            focusCandidate = view
                            foundFullyContainedFocusable = true
                        } else if (viewIsCloserToBoundary)
                            focusCandidate = view
                    }
                }
            }
        }
        return focusCandidate
    }

    /**
     *
     * Handles scrolling in response to a "page up/down" shortcut press. This
     * method will scroll the view by one page up or down and give the focus
     * to the topmost/bottommost component in the new visible area. If no
     * component is a good candidate for focus, this scrollview reclaims the
     * focus.
     *
     * @param direction the scroll direction: [android.view.View.FOCUS_UP]
     * to go one page up or
     * [android.view.View.FOCUS_DOWN] to go one page down
     * @return true if the key event is consumed by this method, false otherwise
     */
    fun pageScroll(direction: Int): Boolean {
        val down = direction == FOCUS_DOWN
        val height = height
        if (down) {
            mTempRect.top = scrollY + height
            val count = childCount
            if (count > 0) {
                val view = getChildAt(count - 1)
                val lp = view.layoutParams as LayoutParams
                val bottom = view.bottom + lp.bottomMargin + paddingBottom

                if (mTempRect.top + height > bottom)
                    mTempRect.top = bottom - height

            }
        } else {
            mTempRect.top = scrollY - height

            if (mTempRect.top < 0)
                mTempRect.top = 0

        }
        mTempRect.bottom = mTempRect.top + height
        return scrollAndFocus(direction, mTempRect.top, mTempRect.bottom)
    }

    /**
     *
     * Handles scrolling in response to a "home/end" shortcut press. This
     * method will scroll the view to the top or bottom and give the focus
     * to the topmost/bottommost component in the new visible area. If no
     * component is a good candidate for focus, this scrollview reclaims the
     * focus.
     *
     * @param direction the scroll direction: [android.view.View.FOCUS_UP]
     * to go the top of the view or
     * [android.view.View.FOCUS_DOWN] to go the bottom
     * @return true if the key event is consumed by this method, false otherwise
     */
    fun fullScroll(direction: Int): Boolean {
        val down = direction == FOCUS_DOWN
        val height = height
        mTempRect.top = 0
        mTempRect.bottom = height
        if (down) {
            val count = childCount
            if (count > 0) {
                val view = getChildAt(count - 1)
                val lp = view.layoutParams as LayoutParams
                mTempRect.bottom = view.bottom + lp.bottomMargin + paddingBottom
                mTempRect.top = mTempRect.bottom - height
            }
        }
        return scrollAndFocus(direction, mTempRect.top, mTempRect.bottom)
    }

    /**
     *
     * Scrolls the view to make the area defined by `top` and
     * `bottom` visible. This method attempts to give the focus
     * to a component visible in this area. If no component can be focused in
     * the new visible area, the focus is reclaimed by this ScrollView.
     *
     * @param direction the scroll direction: [android.view.View.FOCUS_UP]
     * to go upward, [android.view.View.FOCUS_DOWN] to downward
     * @param top       the top offset of the new area to be made visible
     * @param bottom    the bottom offset of the new area to be made visible
     * @return true if the key event is consumed by this method, false otherwise
     */
    private fun scrollAndFocus(direction: Int, top: Int, bottom: Int): Boolean {
        var handled = true
        val height = height
        val containerTop = scrollY
        val containerBottom = containerTop + height
        val up = direction == FOCUS_UP
        var newFocused = findFocusableViewInBounds(up, top, bottom)

        if (newFocused == null)
            newFocused = this

        if (top >= containerTop && bottom <= containerBottom)
            handled = false
        else {
            val delta =
                if (up)
                    top - containerTop
                else
                    bottom - containerBottom
            doScrollY(delta)
        }

        if (newFocused !== findFocus()) newFocused.requestFocus(direction)
        return handled
    }

    /**
     * Handle scrolling in response to an up or down arrow click.
     *
     * @param direction The direction corresponding to the arrow key that was
     * pressed
     * @return True if we consumed the event, false otherwise
     */
    fun arrowScroll(direction: Int): Boolean {
        var currentFocused = findFocus()
        if (currentFocused === this) currentFocused = null
        val nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction)
        val maxJump = maxScrollAmount
        if (nextFocused != null && isWithinDeltaOfScreen(nextFocused, maxJump, height)) {
            nextFocused.getDrawingRect(mTempRect)
            offsetDescendantRectToMyCoords(nextFocused, mTempRect)
            val scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect)
            doScrollY(scrollDelta)
            nextFocused.requestFocus(direction)
        } else {
            // no new focus
            var scrollDelta = maxJump
            if (direction == FOCUS_UP && scrollY < scrollDelta)
                scrollDelta = scrollY
            else if (direction == FOCUS_DOWN) {
                if (childCount > 0) {
                    val child = getChildAt(0)
                    val lp = child.layoutParams as LayoutParams
                    val daBottom = child.bottom + lp.bottomMargin
                    val screenBottom = scrollY + height - paddingBottom
                    scrollDelta = min(daBottom - screenBottom, maxJump)
                }
            }

            if (scrollDelta == 0)
                return false

            doScrollY(if (direction == FOCUS_DOWN) scrollDelta else -scrollDelta)
        }

        if (currentFocused != null && currentFocused.isFocused && isOffScreen(currentFocused)) {
            // previously focused item still has focus and is off screen, give
            // it up (take it back to ourselves)
            // (also, need to temporarily force FOCUS_BEFORE_DESCENDANTS so we are
            // sure to
            // get it)
            val descendantFocusability = descendantFocusability // save
            setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS)
            requestFocus()
            setDescendantFocusability(descendantFocusability) // restore
        }

        return true
    }

    /**
     * @return whether the descendant of this scroll view is scrolled off
     * screen.
     */
    private fun isOffScreen(descendant: View): Boolean =
        !isWithinDeltaOfScreen(descendant, 0, height)

    /**
     * @return whether the descendant of this scroll view is within delta
     * pixels of being on the screen.
     */
    private fun isWithinDeltaOfScreen(descendant: View, delta: Int, height: Int): Boolean {
        descendant.getDrawingRect(mTempRect)
        offsetDescendantRectToMyCoords(descendant, mTempRect)
        return (mTempRect.bottom + delta >= scrollY && mTempRect.top - delta <= scrollY + height)
    }

    /**
     * Smooth scroll by a Y delta
     *
     * @param delta the number of pixels to scroll by on the Y axis
     */
    private fun doScrollY(delta: Int) {
        if (delta != 0) {
            if (isSmoothScrollingEnabled)
                smoothScrollBy(0, delta)
            else
                scrollBy(0, delta)
        }
    }

    /**
     * Like [View.scrollBy], but scroll smoothly instead of immediately.
     *
     * @param x the number of pixels to scroll by on the X axis
     * @param y the number of pixels to scroll by on the Y axis
     */
    @Suppress("MemberVisibilityCanBePrivate", "MemberVisibilityCanBePrivate")
    fun smoothScrollBy(x: Int, y: Int) {
        if (childCount != 0) {
            var dy = y
            val duration = AnimationUtils.currentAnimationTimeMillis() - mLastScroll
            if (duration > ANIMATED_SCROLL_GAP) {
                val child = getChildAt(0)
                val lp = child.layoutParams as LayoutParams
                val childSize = child.height + lp.topMargin + lp.bottomMargin
                val parentSpace = height - paddingTop - paddingBottom
                val scrollY = scrollY
                val maxY = max(0, childSize - parentSpace)
                dy = max(0, min(scrollY + dy, maxY)) - scrollY
                mScroller!!.startScroll(scrollX, scrollY, 0, dy)
                runAnimatedScroll(false)
            } else {
                if (!mScroller!!.isFinished)
                    abortAnimatedScroll()

                scrollBy(x, dy)
            }

            mLastScroll = AnimationUtils.currentAnimationTimeMillis()
        }
    }

    /**
     * Like [.scrollTo], but scroll smoothly instead of immediately.
     *
     * @param x the position where to scroll on the X axis
     * @param y the position where to scroll on the Y axis
     */
    fun smoothScrollTo(x: Int, y: Int) = smoothScrollBy(x - scrollX, y - scrollY)

    /**
     *
     * The scroll range of a scroll view is the overall height of all of its
     * children.
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    override fun computeVerticalScrollRange(): Int {
        val count = childCount
        val parentSpace = height - paddingBottom - paddingTop

        if (count == 0)
            return parentSpace

        val child = getChildAt(0)
        val lp = child.layoutParams as LayoutParams
        var scrollRange = child.bottom + lp.bottomMargin
        val scrollY = scrollY
        val overscrollBottom = max(0, scrollRange - parentSpace)

        if (scrollY < 0)
            scrollRange -= scrollY
        else if (scrollY > overscrollBottom)
            scrollRange += scrollY - overscrollBottom

        return scrollRange
    }

    /** @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    override fun computeVerticalScrollOffset(): Int = max(0, super.computeVerticalScrollOffset())

    /** @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    override fun computeVerticalScrollExtent(): Int = super.computeVerticalScrollExtent()

    /** @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    override fun computeHorizontalScrollRange(): Int = super.computeHorizontalScrollRange()

    /** @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    override fun computeHorizontalScrollOffset(): Int = super.computeHorizontalScrollOffset()

    /** @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    override fun computeHorizontalScrollExtent(): Int = super.computeHorizontalScrollExtent()


    override fun measureChild(child: View, parentWidthMeasureSpec: Int, parentHeightMeasureSpec: Int) {
        val lp = child.layoutParams
        val childWidthMeasureSpec: Int = getChildMeasureSpec(parentWidthMeasureSpec, paddingLeft + paddingRight, lp.width)
        val childHeightMeasureSpec: Int = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun measureChildWithMargins(child: View, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int) {
        val lp = child.layoutParams as MarginLayoutParams

        val childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin + widthUsed, lp.width)

        val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED)

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun computeScroll() {
        if (!mScroller!!.isFinished) {
            mScroller!!.computeScrollOffset()
            val y = mScroller!!.currY
            var unconsumed = y - scrollY

            // Nested Scrolling Pre Pass
            mScrollConsumed[1] = 0
            dispatchNestedPreScroll(0, unconsumed, mScrollConsumed, null, ViewCompat.TYPE_NON_TOUCH)
            unconsumed -= mScrollConsumed[1]
            val range = scrollRange

            if (unconsumed != 0) {
                // Internal Scroll
                val oldScrollY = scrollY
                overScrollByCompat(unconsumed, scrollX, oldScrollY, range, 0, 0, false)
                val scrolledByMe = scrollY - oldScrollY
                unconsumed -= scrolledByMe

                // Nested Scrolling Post Pass
                mScrollConsumed[1] = 0
                dispatchNestedScroll(0, scrolledByMe, 0, unconsumed, mScrollOffset, ViewCompat.TYPE_NON_TOUCH, mScrollConsumed)

                unconsumed -= mScrollConsumed[1]
            }
            if (unconsumed != 0) {
                val mode = overScrollMode
                val canOverscroll = (mode == OVER_SCROLL_ALWAYS || (mode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0))
                if (canOverscroll) {
                    ensureGlows()

                    if (unconsumed < 0 && mEdgeGlowTop!!.isFinished)
                        mEdgeGlowTop!!.onAbsorb(mScroller!!.currVelocity.toInt())
                    else if (mEdgeGlowBottom!!.isFinished)
                        mEdgeGlowBottom!!.onAbsorb(mScroller!!.currVelocity.toInt())

                }

                abortAnimatedScroll()
            }

            if (!mScroller!!.isFinished)
                ViewCompat.postInvalidateOnAnimation(this)

        }
    }

    private fun runAnimatedScroll(participateInNestedScrolling: Boolean) {
        if (participateInNestedScrolling)
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
        else
            stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)

        ViewCompat.postInvalidateOnAnimation(this)
    }

    private fun abortAnimatedScroll() {
        mScroller!!.abortAnimation()
        stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
    }

    /**
     * Scrolls the view to the given child.
     *
     * @param child the View to scroll to
     */
    private fun scrollToChild(child: View) {
        child.getDrawingRect(mTempRect)

        /* Offset from child's local coordinates to ScrollView coordinates */
        offsetDescendantRectToMyCoords(child, mTempRect)

        val scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect)

        if (scrollDelta != 0)
            scrollBy(0, scrollDelta)

    }

    /**
     * If rect is off screen, scroll just enough to get it (or at least the
     * first screen size chunk of it) on screen.
     *
     * @param rect      The rectangle.
     * @param immediate True to scroll immediately without animation
     * @return true if scrolling was performed
     */
    private fun scrollToChildRect(rect: Rect, immediate: Boolean): Boolean {
        val delta = computeScrollDeltaToGetChildRectOnScreen(rect)
        val scroll = delta != 0
        if (scroll)
            if (immediate)
                scrollBy(0, delta)
            else
                smoothScrollBy(0, delta)

        return scroll
    }

    /**
     * Compute the amount to scroll in the Y direction in order to get
     * a rectangle completely on the screen (or, if taller than the screen,
     * at least the first screen size chunk of it).
     *
     * @param rect The rect.
     * @return The scroll delta.
     */
    private fun computeScrollDeltaToGetChildRectOnScreen(rect: Rect): Int {
        if (childCount == 0) return 0
        val height = height
        var screenTop = scrollY
        var screenBottom = screenTop + height
        val actualScreenBottom = screenBottom
        val fadingEdge = verticalFadingEdgeLength

        // TODO: screenTop should be incremented by fadingEdge * getTopFadingEdgeStrength (but for
        // the target scroll distance).
        // leave room for top fading edge as long as rect isn't at very top
        if (rect.top > 0)
            screenTop += fadingEdge

        // TODO: screenBottom should be decremented by fadingEdge * getBottomFadingEdgeStrength (but
        // for the target scroll distance).
        // leave room for bottom fading edge as long as rect isn't at very bottom
        val child = getChildAt(0)
        val lp = child.layoutParams as LayoutParams
        if (rect.bottom < child.height + lp.topMargin + lp.bottomMargin)
            screenBottom -= fadingEdge

        var scrollYDelta = 0
        if (rect.bottom > screenBottom && rect.top > screenTop) {

            // need to move down to get it in view: move down just enough so
            // that the entire rectangle is in view (or at least the first
            // screen size chunk).

            scrollYDelta += if (rect.height() > height) // just enough to get screen size chunk on
                (rect.top - screenTop)
            else // get entire rect at bottom of screen
                (rect.bottom - screenBottom)

            // make sure we aren't scrolling beyond the end of our content
            val bottom = child.bottom + lp.bottomMargin
            val distanceToBottom = bottom - actualScreenBottom
            scrollYDelta = min(scrollYDelta, distanceToBottom)
        } else if (rect.top < screenTop && rect.bottom < screenBottom) {
            // need to move up to get it in view: move up just enough so that
            // entire rectangle is in view (or at least the first screen
            // size chunk of it).

            scrollYDelta -= if (rect.height() > height) // screen size chunk
                (screenBottom - rect.bottom)
            else // entire rect at top
                (screenTop - rect.top)

            // make sure we aren't scrolling any further than the top our content
            scrollYDelta = max(scrollYDelta, -scrollY)
        }
        return scrollYDelta
    }

    override fun requestChildFocus(child: View, focused: View) {
        if (!mIsLayoutDirty)
            scrollToChild(focused)
        else // The child may not be laid out yet, we can't compute the scroll yet
            mChildToScrollTo = focused

        super.requestChildFocus(child, focused)
    }


    override fun onRequestFocusInDescendants(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        if (previouslyFocusedRect != null) {
            var mDirection = direction

            if (mDirection == FOCUS_FORWARD)
                mDirection = FOCUS_DOWN
            else if (direction == FOCUS_BACKWARD)
                mDirection = FOCUS_UP

            val nextFocus = FocusFinder.getInstance().findNextFocusFromRect(this, previouslyFocusedRect, mDirection)
                ?: return false
            return if (isOffScreen(nextFocus)) false
            else nextFocus.requestFocus(mDirection, previouslyFocusedRect)
        }
        return false
    }

    override fun requestChildRectangleOnScreen(child: View, rectangle: Rect, immediate: Boolean): Boolean {
        // offset into coordinate space of this scroll view
        rectangle.offset(child.left - child.scrollX, child.top - child.scrollY)
        return scrollToChildRect(rectangle, immediate)
    }

    override fun requestLayout() {
        mIsLayoutDirty = true
        super.requestLayout()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        mIsLayoutDirty = false
        // Give a child focus if it needs it
        if (mChildToScrollTo != null && isViewDescendantOf(mChildToScrollTo!!, this))
            scrollToChild(mChildToScrollTo!!)

        mChildToScrollTo = null
        if (!mIsLaidOut) {
            // If there is a saved state, scroll to the position saved in that state.
            if (mSavedState != null) {
                scrollTo(scrollX, mSavedState!!.scrollPosition)
                mSavedState = null
            } // mScrollY default value is "0"

            // Make sure current scrollY position falls into the scroll range.  If it doesn't,
            // scroll such that it does.
            var childSize = 0
            if (childCount > 0) {
                val child = getChildAt(0)
                val lp = child.layoutParams as LayoutParams
                childSize = child.measuredHeight + lp.topMargin + lp.bottomMargin
            }
            val parentSpace = b - t - paddingTop - paddingBottom
            val currentScrollY = scrollY
            val newScrollY = clamp(currentScrollY, parentSpace, childSize)

            if (newScrollY != currentScrollY)
                scrollTo(scrollX, newScrollY)
        }

        // Calling this with the present values causes it to re-claim them
        scrollTo(scrollX, scrollY)
        mIsLaidOut = true
    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mIsLaidOut = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val currentFocused = findFocus()

        if (currentFocused != null && this !== currentFocused) {
            // If the currently-focused view was visible on the screen when the
            // screen was at the old height, then scroll the screen to make that
            // view visible with the new screen height.
            if (isWithinDeltaOfScreen(currentFocused, 0, oldh)) {
                currentFocused.getDrawingRect(mTempRect)
                offsetDescendantRectToMyCoords(currentFocused, mTempRect)
                val scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect)
                doScrollY(scrollDelta)
            }
        }
    }

    /**
     * Fling the scroll view
     *
     * @param velocityY The initial velocity in the Y direction. Positive
     * numbers mean that the finger/cursor is moving down the screen,
     * which means we want to scroll towards the top.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun fling(velocityY: Int) {
        if (childCount > 0) {
            // overscroll
            mScroller!!.fling(scrollX, scrollY, 0, velocityY, 0, 0, Int.MIN_VALUE, Int.MAX_VALUE, 0, 0)
            runAnimatedScroll(true)
        }
    }

    private fun endDrag() {
        mIsBeingDragged = false
        recycleVelocityTracker()
        stopNestedScroll(ViewCompat.TYPE_TOUCH)

        mEdgeGlowTop?.onRelease()
        mEdgeGlowBottom?.onRelease()
    }

    /**
     * {@inheritDoc}
     *
     *
     * This version also clamps the scrolling to the bounds of our child.
     */
    override fun scrollTo(a: Int, b: Int) {
        // we rely on the fact the View.scrollBy calls scrollTo.
        var x = a
        var y = b
        if (childCount > 0) {
            val child = getChildAt(0)
            val lp = child.layoutParams as LayoutParams
            val parentSpaceHorizontal = width - paddingLeft - paddingRight
            val childSizeHorizontal = child.width + lp.leftMargin + lp.rightMargin
            val parentSpaceVertical = height - paddingTop - paddingBottom
            val childSizeVertical = child.height + lp.topMargin + lp.bottomMargin
            x = clamp(x, parentSpaceHorizontal, childSizeHorizontal)
            y = clamp(y, parentSpaceVertical, childSizeVertical)

            if (x != scrollX || y != scrollY)
                super.scrollTo(x, y)

        }
    }

    private fun ensureGlows() {
        if (overScrollMode != OVER_SCROLL_NEVER) {
            if (mEdgeGlowTop == null) {
                val context = context
                mEdgeGlowTop = BouncyEdgeEffect(context, spring, this, EdgeEffectFactory.DIRECTION_TOP, flingAnimationSize, overscrollAnimationSize)
                mEdgeGlowBottom = BouncyEdgeEffect(context, spring, this, EdgeEffectFactory.DIRECTION_BOTTOM, flingAnimationSize, overscrollAnimationSize)
            }
        } else {
            mEdgeGlowTop = null
            mEdgeGlowBottom = null
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (mEdgeGlowTop != null) {
            val scrollY = scrollY
            if (!mEdgeGlowTop!!.isFinished) {
                val restoreCount = canvas.save()
                val width = width
                var height = height
                val xTranslation = 0
                var yTranslation = min(0, scrollY)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && clipToPadding) {
                    height -= paddingTop + paddingBottom
                    yTranslation += paddingTop
                }
                canvas.translate(xTranslation.toFloat(), yTranslation.toFloat())

                mEdgeGlowTop!!.setSize(width, height)

                if (mEdgeGlowTop!!.draw(canvas))
                    ViewCompat.postInvalidateOnAnimation(this)

                canvas.restoreToCount(restoreCount)
            }
            if (!mEdgeGlowBottom!!.isFinished) {
                val restoreCount = canvas.save()
                val width = width
                var height = height
                val xTranslation = 0
                var yTranslation = max(scrollRange, scrollY) + height
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && clipToPadding) {
                    height -= paddingTop + paddingBottom
                    yTranslation -= paddingBottom
                }
                canvas.translate((xTranslation - width).toFloat(), yTranslation.toFloat())
                canvas.rotate(180f, width.toFloat(), 0f)
                mEdgeGlowBottom!!.setSize(width, height)

                if (mEdgeGlowBottom!!.draw(canvas))
                    ViewCompat.postInvalidateOnAnimation(this)

                canvas.restoreToCount(restoreCount)
            }
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        mSavedState = state
        requestLayout()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.scrollPosition = scrollY
        return ss
    }

    internal class SavedState : BaseSavedState {
        var scrollPosition = 0

        constructor(superState: Parcelable?) : super(superState)

        @Suppress("unused")
        constructor(source: Parcel) : super(source) {
            scrollPosition = source.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(scrollPosition)
        }

        override fun toString(): String =
            ("HorizontalScrollView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " scrollPosition=" + scrollPosition + "}")


        @Suppress("unused")
        val creator: Creator<SavedState?> = object : Creator<SavedState?> {
            override fun createFromParcel(`in`: Parcel): SavedState = SavedState(`in`)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    internal class AccessibilityDelegate : AccessibilityDelegateCompat() {
        override fun performAccessibilityAction(host: View, action: Int, arguments: Bundle): Boolean {
            if (super.performAccessibilityAction(host, action, arguments))
                return true


            val nsvHost = host as BouncyNestedScrollView

            if (!nsvHost.isEnabled)
                return false

            when (action) {
                AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD -> {
                    run {
                        val viewportHeight: Int = (nsvHost.height - nsvHost.paddingBottom - nsvHost.paddingTop)

                        val targetScrollY: Int = min(nsvHost.scrollY + viewportHeight, nsvHost.scrollRange)

                        if (targetScrollY != nsvHost.scrollY) {
                            nsvHost.smoothScrollTo(0, targetScrollY)
                            return true
                        }
                    }

                    return false
                }
                AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD -> {
                    run {
                        val viewportHeight: Int = (nsvHost.height - nsvHost.paddingBottom - nsvHost.paddingTop)
                        val targetScrollY: Int = max(nsvHost.scrollY - viewportHeight, 0)
                        if (targetScrollY != nsvHost.scrollY) {
                            nsvHost.smoothScrollTo(0, targetScrollY)
                            return true
                        }
                    }
                    return false
                }
            }
            return false
        }

        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            val nsvHost = host as BouncyNestedScrollView
            info.className = ScrollView::class.java.name
            if (nsvHost.isEnabled) {
                val scrollRange = nsvHost.scrollRange
                if (scrollRange > 0) {
                    info.isScrollable = true
                    if (nsvHost.scrollY > 0)
                        info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)

                    if (nsvHost.scrollY < scrollRange)
                        info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)

                }
            }
        }

        override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
            super.onInitializeAccessibilityEvent(host, event)
            val nsvHost = host as BouncyNestedScrollView
            event.className = ScrollView::class.java.name
            val scrollable = nsvHost.scrollRange > 0
            event.isScrollable = scrollable
            event.scrollX = nsvHost.scrollX
            event.scrollY = nsvHost.scrollY
            AccessibilityRecordCompat.setMaxScrollX(event, nsvHost.scrollX)
            AccessibilityRecordCompat.setMaxScrollY(event, nsvHost.scrollRange)
        }
    }

    companion object {
        private const val ANIMATED_SCROLL_GAP = 250
        private const val MAX_SCROLL_FACTOR = 0.5f
        private const val TAG = "BouncyNestedScrollView"

        /**
         * Sentinel value for no current active pointer.
         * Used by [.mActivePointerId].
         */
        private const val INVALID_POINTER = -1
        private val ACCESSIBILITY_DELEGATE = AccessibilityDelegate()
        private val SCROLLVIEW_STYLEABLE = intArrayOf(android.R.attr.fillViewport)

        /**
         * Return true if child is a descendant of parent, (or equal to the parent).
         */
        private fun isViewDescendantOf(child: View, parent: View): Boolean {
            return when {
                child === parent -> true
                else -> {
                    val theParent = child.parent
                    (theParent is ViewGroup) && isViewDescendantOf(theParent as View, parent)
                }
            }
        }

        private fun clamp(n: Int, my: Int, child: Int): Int {
            return if (my >= child || n < 0) 0
            else if ((my + n) > child) child - my
            else n
        }
    }

    init {
        initScrollView()

        //read attributes
        context.obtainStyledAttributes(attrs, SCROLLVIEW_STYLEABLE, defStyleAttr, 0)
                .apply {
                    isFillViewport = getBoolean(0, false)
                    recycle()
                }
        context.theme.obtainStyledAttributes(attrs, R.styleable.BouncyNestedScrollView, 0, 0)
                .apply {
                    overscrollAnimationSize = getFloat(R.styleable.BouncyNestedScrollView_overscroll_animation_size, 0.5f)
                    flingAnimationSize = getFloat(R.styleable.BouncyNestedScrollView_fling_animation_size, 0.5f)

                    when (getInt(R.styleable.BouncyNestedScrollView_damping_ratio, 0)) {
                        0 -> dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                        1 -> dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
                        2 -> dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                        3 -> dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                    }

                    when (getInt(R.styleable.BouncyNestedScrollView_stiffness, 1)) {
                        0 -> stiffness = SpringForce.STIFFNESS_VERY_LOW
                        1 -> stiffness = SpringForce.STIFFNESS_LOW
                        2 -> stiffness = SpringForce.STIFFNESS_MEDIUM
                        3 -> stiffness = SpringForce.STIFFNESS_HIGH
                    }
                    recycle()
                }

        mParentHelper = NestedScrollingParentHelper(this)
        mChildHelper = NestedScrollingChildHelper(this)

        // ...because why else would you be using this widget?
        isNestedScrollingEnabled = true
        ViewCompat.setAccessibilityDelegate(this, ACCESSIBILITY_DELEGATE)
    }


}