package app.simple.inure.decorations.overscroll

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.widget.EdgeEffect
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.overscroll.RecyclerViewConstants.flingTranslationMagnitude
import app.simple.inure.decorations.overscroll.RecyclerViewConstants.overScrollRotationMagnitude
import app.simple.inure.decorations.overscroll.RecyclerViewConstants.overScrollTranslationMagnitude
import app.simple.inure.decorations.theme.ThemeRecyclerView
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.StatusBarHeight

/**
 * Custom recycler view with nice layout animation and
 * smooth overscroll effect and various states retention
 */
class CustomVerticalRecyclerView(context: Context, attrs: AttributeSet?) : ThemeRecyclerView(context, attrs) {

    private var manuallyAnimated = false
    private var fastScroll = true
    private var isEdgeColorRequired = true
    private var isFastScrollerAdded = false

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomRecyclerView, 0, 0).apply {
            try {
                if (getBoolean(R.styleable.CustomRecyclerView_statusBarPaddingRequired, true)) {
                    if (!AppearancePreferences.isTransparentStatusDisabled()) {
                        setPadding(paddingLeft, StatusBarHeight.getStatusBarHeight(resources) + paddingTop, paddingRight, paddingBottom)
                    }
                }

                fastScroll = getBoolean(R.styleable.CustomRecyclerView_isFastScrollRequired, true)
                manuallyAnimated = getBoolean(R.styleable.CustomRecyclerView_manuallyAnimated, false)
                isEdgeColorRequired = getBoolean(R.styleable.CustomRecyclerView_isEdgeColorRequired, true)

                if (AccessibilityPreferences.isAnimationReduced())
                    layoutAnimation = null
            } finally {
                recycle()
            }
        }

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        setHasFixedSize(true)

        if (AccessibilityPreferences.isDividerEnabled()) {
            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)

            divider.setDrawable(ShapeDrawable().apply {
                intrinsicHeight = 1
                paint.color = ThemeManager.theme.viewGroupTheme.dividerBackground
            })

            addItemDecoration(divider)
        }

        this.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
            override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
                return object : EdgeEffect(recyclerView.context) {
                    override fun onPull(deltaDistance: Float) {
                        super.onPull(deltaDistance)
                        handlePull(deltaDistance)
                        setEdgeColor()
                    }

                    override fun onPull(deltaDistance: Float, displacement: Float) {
                        super.onPull(deltaDistance, displacement)
                        handlePull(deltaDistance)
                        setEdgeColor()
                    }

                    private fun handlePull(deltaDistance: Float) {
                        /**
                         * This is called on every touch event while the list is scrolled with a finger.
                         * simply update the view properties without animation.
                         */
                        val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                        val rotationDelta = sign * deltaDistance * overScrollRotationMagnitude

                        /**
                         * This value decides how fast the recycler view views should move when
                         * they're being overscrolled. Often it is determined using the area of the
                         * recycler view because its length is how far the finger can move hence
                         * the overscroll value.
                         */
                        // val overscrollLengthConst = if (isLandscape) recyclerView.height else recyclerView.height / 2
                        val translationYDelta = sign * recyclerView.height / 2 * deltaDistance * overScrollTranslationMagnitude

                        recyclerView.forEachVisibleHolder { holder: VerticalListViewHolder ->
                            holder.rotation.cancel()
                            holder.translationY.cancel()
                            holder.itemView.rotation += rotationDelta
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
                            holder.rotation.start()
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
                        if (!isEdgeColorRequired) {
                            color = ThemeManager.theme.viewGroupTheme.background
                        }
                    }
                }
            }
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        try {
            adapter?.stateRestorationPolicy = Adapter.StateRestorationPolicy.ALLOW
        } catch (e: UnsupportedOperationException) {
            e.printStackTrace()
        }

        if (this.adapter.isNotNull()) {
            layoutAnimation = null
        }

        super.setAdapter(adapter)

        if (!manuallyAnimated) {
            if (!AccessibilityPreferences.isAnimationReduced())
                scheduleLayoutAnimation()
        }

        /**
         * Setup fast scroller only when adapter is large enough
         * to require a fast scroller
         */
        if (adapter!!.itemCount > 25 && fastScroll && !isFastScrollerAdded) {
            setupFastScroller()
        }
    }

    /**
     * Setup fast scroller if needed
     */
    private fun setupFastScroller() {
        FastScrollerBuilder(this).build()
        isFastScrollerAdded = true
    }

    private inline fun <reified T : VerticalListViewHolder> RecyclerView.forEachVisibleHolder(action: (T) -> Unit) {
        for (i in 0 until childCount) {
            action(getChildViewHolder(getChildAt(i)) as T)
        }
    }
}