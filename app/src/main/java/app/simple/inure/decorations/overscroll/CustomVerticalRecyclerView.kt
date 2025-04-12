package app.simple.inure.decorations.overscroll

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.EdgeEffect
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import app.simple.inure.R
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.itemdecorations.DividerItemDecoration
import app.simple.inure.decorations.theme.ThemeRecyclerView
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.RecyclerViewUtils.flingTranslationMagnitude
import app.simple.inure.util.RecyclerViewUtils.overScrollTranslationMagnitude
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.ViewUtils.navigationEdgeToEdge
import app.simple.inure.util.ViewUtils.statusBarEdgeToEdge

/**
 * Custom recycler view with nice layout animation and
 * smooth overscroll effect and various states retention
 */
open class CustomVerticalRecyclerView(context: Context, attrs: AttributeSet?) : ThemeRecyclerView(context, attrs),
                                                                                DynamicAnimation.OnAnimationUpdateListener {

    private var manuallyAnimated = false
    private var fastScroll = true
    private var isEdgeColorRequired = true
    private var isFastScrollerAdded = false
    private var isTopFadingEdge = true

    private var dividerItemDecoration: DividerItemDecoration? = null
    private var fastScrollerBuilder: FastScrollerBuilder? = null

    private var edgeColor = 0

    init {
        if (isInEditMode.invert()) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.RecyclerView, 0, 0).apply {
                try {
                    edgeColor = AppearancePreferences.getAccentColor()

                    if (getBoolean(R.styleable.RecyclerView_statusBarPaddingRequired, true)) {
                        statusBarEdgeToEdge()
                    }

                    navigationEdgeToEdge()

                    if (getBoolean(R.styleable.RecyclerView_isFadingEdgeRequired, false)) {
                        isVerticalFadingEdgeEnabled = true
                        setFadingEdgeLength(StatusBarHeight.getStatusBarHeight(resources) + paddingTop)
                    }

                    isTopFadingEdge = getBoolean(R.styleable.RecyclerView_isTopFadingEdgeOnly, true)

                    fastScroll = getBoolean(R.styleable.RecyclerView_isFastScrollRequired, true)
                    manuallyAnimated = getBoolean(R.styleable.RecyclerView_manuallyAnimated, false)
                    isEdgeColorRequired = getBoolean(R.styleable.RecyclerView_isEdgeColorRequired, true)

                    if (AccessibilityPreferences.isAnimationReduced()) {
                        layoutAnimation = null
                    }
                } finally {
                    recycle()
                }
            }

            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = !AccessibilityPreferences.isAnimationReduced()
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)

            addDividers()

            this.edgeEffectFactory = object : EdgeEffectFactory() {
                override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
                    return object : EdgeEffect(recyclerView.context) {
                        override fun onPull(deltaDistance: Float) {
                            super.onPull(deltaDistance)
                            handlePull(deltaDistance)
                            setEdgeColor()
                            // clearDividerDecorations()
                        }

                        override fun onPull(deltaDistance: Float, displacement: Float) {
                            super.onPull(deltaDistance, displacement)
                            handlePull(deltaDistance)
                            setEdgeColor()
                            // clearDividerDecorations()
                        }

                        private fun handlePull(deltaDistance: Float) {
                            /**
                             * This is called on every touch event while the list is scrolled with a finger.
                             * simply update the view properties without animation.
                             */
                            val sign = if (direction == DIRECTION_BOTTOM) -1 else 1

                            /**
                             * This value decides how fast the recycler view views should move when
                             * they're being overscrolled. Often it is determined using the area of the
                             * recycler view because its length is how far the finger can move hence
                             * the overscroll value.
                             */
                            // val overscrollLengthConst = if (isLandscape) recyclerView.height else recyclerView.height / 2
                            val translationYDelta = sign * recyclerView.height / 2 * deltaDistance * overScrollTranslationMagnitude

                            recyclerView.forEachVisibleHolder { holder: VerticalListViewHolder ->
                                holder.translationY.cancel()
                                holder.itemView.translationY += translationYDelta
                            }
                        }

                        override fun onRelease() {
                            super.onRelease()
                            setEdgeColor()
                            /**
                             * The finger is lifted. This is when we should start the animations to bring
                             * the view property values back to their resting states.
                             */
                            recyclerView.forEachVisibleHolder { holder: VerticalListViewHolder ->
                                try {
                                    holder.translationY.cancel()
                                    holder.translationY.removeUpdateListener(this@CustomVerticalRecyclerView)
                                } catch (e: UnsupportedOperationException) {
                                    Log.e("CustomVerticalRecyclerView", "onRelease: ", e)
                                }

                                try {
                                    holder.translationY.addUpdateListener(this@CustomVerticalRecyclerView)
                                } catch (e: UnsupportedOperationException) {
                                    Log.e("CustomVerticalRecyclerView", "onRelease: ${e.message}")
                                }

                                holder.translationY.start()
                            }
                        }

                        override fun onAbsorb(velocity: Int) {
                            super.onAbsorb(velocity)
                            setEdgeColor()
                            val sign = if (direction == DIRECTION_BOTTOM) -1 else 1

                            /**
                             * The list has reached the edge on fling
                             */
                            val translationVelocity = sign * velocity * flingTranslationMagnitude
                            recyclerView.forEachVisibleHolder { holder: VerticalListViewHolder ->
                                try {
                                    holder.translationY.cancel()
                                    holder.translationY.removeUpdateListener(this@CustomVerticalRecyclerView)
                                } catch (e: UnsupportedOperationException) {
                                    Log.e("CustomVerticalRecyclerView", "onRelease: ", e)
                                }

                                try {
                                    holder.translationY.addUpdateListener(this@CustomVerticalRecyclerView)
                                } catch (e: UnsupportedOperationException) {
                                    Log.e("CustomVerticalRecyclerView", "onRelease: ${e.message}")
                                }

                                holder.translationY
                                    .setStartVelocity(translationVelocity)
                                    .start()
                            }
                        }

                        /**
                         * Have to call from all [EdgeEffect.onPull], [EdgeEffect.onRelease],
                         * [EdgeEffect.onAbsorb] functions to make sure the edge colors don't appear
                         * on non-required places. This is how it is but works.
                         */
                        private fun setEdgeColor() {
                            @Suppress("LiftReturnOrAssignment")
                            if (!isEdgeColorRequired) {
                                color = ThemeManager.theme.viewGroupTheme.background
                            } else {
                                color = edgeColor
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addDividers() {
        if (AccessibilityPreferences.isDividerEnabled()) {
            dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)

            dividerItemDecoration!!.setDrawable(ShapeDrawable().apply {
                intrinsicHeight = 1
                paint.color = ThemeManager.theme.viewGroupTheme.dividerBackground
            })

            addItemDecoration(dividerItemDecoration!!)
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        try {
            adapter?.stateRestorationPolicy = Adapter.StateRestorationPolicy.ALLOW
        } catch (e: UnsupportedOperationException) {
            e.printStackTrace()
        }

        if (this.adapter.isNotNull()) {
            this.animate()
                .alpha(0f)
                .setDuration(250)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    super.setAdapter(adapter)
                    this.animate()
                        .alpha(1f)
                        .setInterpolator(DecelerateInterpolator())
                        .setDuration(150)
                        .start()
                }.start()
        } else {
            super.setAdapter(adapter)
        }

        if (!manuallyAnimated && isInEditMode.invert()) {
            if (!AccessibilityPreferences.isAnimationReduced()) {
                scheduleLayoutAnimation()
            }
        }

        /**
         * Setup fast scroller only when adapter is large enough
         * to require a fast scroller
         */
        adapter?.let {
            if (adapter.itemCount > 0 && fastScroll && !isFastScrollerAdded) {
                setupFastScroller()
            }
        }
    }

    fun setExclusiveAdapter(adapter: Adapter<*>?) {
        if (this.adapter == null) {
            setAdapter(adapter)
            if (AccessibilityPreferences.isAnimationReduced().invert()) {
                scheduleLayoutAnimation()
            }
        } else {
            swapAdapter(adapter, false)
        }
    }

    override fun isPaddingOffsetRequired(): Boolean {
        return isTopFadingEdge
    }

    override fun getTopPaddingOffset(): Int {
        return -paddingTop
    }

    override fun getBottomPaddingOffset(): Int {
        return paddingBottom
    }

    override fun getBottomFadingEdgeStrength(): Float {
        return if (isTopFadingEdge) {
            0f
        } else {
            super.getBottomFadingEdgeStrength()
        }
    }

    /**
     * Setup fast scroller if needed
     */
    private fun setupFastScroller() {
        fastScrollerBuilder = FastScrollerBuilder(this)
        fastScrollerBuilder?.build()
        isFastScrollerAdded = true
    }

    private inline fun <reified T : VerticalListViewHolder> RecyclerView.forEachVisibleHolder(action: (T) -> Unit) {
        for (i in 0 until childCount) {
            action(getChildViewHolder(getChildAt(i)) as T)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.ACCENT_COLOR -> {
                edgeColor = AppearancePreferences.getAccentColor()
                fastScrollerBuilder?.updateAesthetics()
            }
        }
    }

    override fun onAnimationUpdate(animation: DynamicAnimation<*>?, value: Float, velocity: Float) {
        invalidateItemDecorations()
    }
}
