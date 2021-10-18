package app.simple.inure.decorations.views

import android.content.Context
import android.util.AttributeSet
import android.widget.EdgeEffect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.viewholders.HorizontalListViewHolder
import app.simple.inure.util.StatusBarHeight

/**
 * Custom recycler view with nice layout animation and
 * smooth overscroll effect and various states retention
 */
class CustomHorizontalRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    private var isLandscape = false

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomRecyclerView, 0, 0).apply {
            try {
                if (getBoolean(R.styleable.CustomRecyclerView_statusBarPaddingRequired, true)) {
                    setPadding(paddingLeft, StatusBarHeight.getStatusBarHeight(resources) + paddingTop, paddingRight, paddingBottom)
                }

                isLandscape = StatusBarHeight.isLandscape(context)
            } finally {
                recycle()
            }
        }

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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

                    override fun onRelease() {
                        super.onRelease()
                        /**
                         * The finger is lifted. This is when we should start the animations to bring
                         * the view property values back to their resting states.
                         */
                        recyclerView.forEachVisibleHolder { holder: HorizontalListViewHolder ->
                            holder.rotation.start()
                            holder.translationX.start()
                        }
                    }

                    override fun onAbsorb(velocity: Int) {
                        super.onAbsorb(velocity)
                        val sign = if (direction == DIRECTION_RIGHT) -1 else 1

                        /**
                         * The list has reached the edge on fling
                         */
                        val translationVelocity = sign * velocity * flingTranslationMagnitude
                        recyclerView.forEachVisibleHolder { holder: HorizontalListViewHolder ->
                            holder.translationX
                                    .setStartVelocity(translationVelocity)
                                    .start()
                        }
                    }

                    private fun handlePull(deltaDistance: Float) {
                        /**
                         * This is called on every touch event while the list is scrolled with a finger.
                         * simply update the view properties without animation.
                         */
                        println(direction)
                        val sign = if (direction == DIRECTION_RIGHT) 1 else -1
                        val rotationDelta = sign * deltaDistance * overScrollRotationMagnitude

                        /**
                         * This value decide how fast the recycler view views should move when
                         * they're being overscrolled. Often it is determined using the area of the
                         * recycler view because its length is how far the finger can move hence
                         * the overscroll value.
                         */
                        val overscrollLengthConst = if (isLandscape) recyclerView.height else recyclerView.height * 2
                        val translationXDelta = sign * overscrollLengthConst * deltaDistance * overScrollTranslationMagnitude

                        recyclerView.forEachVisibleHolder { holder: HorizontalListViewHolder ->
                            holder.rotation.cancel()
                            holder.translationX.cancel()
                            holder.itemView.rotation += rotationDelta
                            holder.itemView.translationX -= translationXDelta
                        }
                    }
                }
            }
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.stateRestorationPolicy = Adapter.StateRestorationPolicy.ALLOW
        scheduleLayoutAnimation()
    }

    private inline fun <reified T : HorizontalListViewHolder> RecyclerView.forEachVisibleHolder(action: (T) -> Unit) {
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
