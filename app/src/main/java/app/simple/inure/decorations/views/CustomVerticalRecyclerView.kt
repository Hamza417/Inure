package app.simple.inure.decorations.views

import android.content.Context
import android.util.AttributeSet
import android.widget.EdgeEffect
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.util.StatusBarHeight

/**
 * Custom recycler view with nice layout animation and
 * smooth overscroll effect and various states retention
 */
class CustomVerticalRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    private var manuallyAnimated = false

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomRecyclerView, 0, 0).apply {
            try {
                if (getBoolean(R.styleable.CustomRecyclerView_statusBarPaddingRequired, true)) {
                    setPadding(paddingLeft, StatusBarHeight.getStatusBarHeight(resources) + paddingTop, paddingRight, paddingBottom)
                }

                manuallyAnimated = getBoolean(R.styleable.CustomRecyclerView_manuallyAnimated, false)
            } finally {
                recycle()
            }
        }

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        setHasFixedSize(true)

        this.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
            override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {

                return object : EdgeEffect(recyclerView.context) {
                    override fun onPull(deltaDistance: Float) {
                        super.onPull(deltaDistance)
                        handlePull(deltaDistance)
                    }

                    override fun onPull(deltaDistance: Float, displacement: Float) {
                        super.onPull(deltaDistance, displacement)
                        handlePull(deltaDistance)
                    }

                    private fun handlePull(deltaDistance: Float) {
                        /**
                         * This is called on every touch event while the list is scrolled with a finger.
                         * simply update the view properties without animation.
                         */
                        val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                        val rotationDelta = sign * deltaDistance * overScrollRotationMagnitude
                        val translationYDelta = sign * recyclerView.width * deltaDistance * overScrollTranslationMagnitude

                        recyclerView.forEachVisibleHolder { holder: VerticalListViewHolder ->
                            holder.rotation.cancel()
                            holder.translationY.cancel()
                            holder.itemView.rotation += rotationDelta
                            holder.itemView.translationY += translationYDelta
                        }
                    }

                    override fun onRelease() {
                        super.onRelease()
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
                }
            }
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.stateRestorationPolicy = Adapter.StateRestorationPolicy.ALLOW

        if (!manuallyAnimated) {
            scheduleLayoutAnimation()
        }

        /**
         * Setup fast scroller only when adapter is large enough
         * to require a fast scroller
         */
        if (adapter!!.itemCount > 25) {
            setupFastScroller()
        }
    }

    /**
     * Setup fast scroller if needed
     */
    fun setupFastScroller() {
        FastScrollerBuilder(this)
                .useMd2Style()
                .setTrackDrawable(ResourcesCompat.getDrawable(resources, R.drawable.afs_md2_track, context.theme)!!)
                .setThumbDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_thumb, context.theme)!!)
                .build()
    }

    private inline fun <reified T : VerticalListViewHolder> RecyclerView.forEachVisibleHolder(action: (T) -> Unit) {
        for (i in 0 until childCount) {
            action(getChildViewHolder(getChildAt(i)) as T)
        }
    }

    companion object {
        private const val value = 1.0f
        const val flingTranslationMagnitude = value
        const val overScrollRotationMagnitude = value
        const val overScrollTranslationMagnitude = value
    }
}
