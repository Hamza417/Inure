package app.simple.inure.decorations.viewholders

import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.preferences.AppearancePreferences

open class VerticalListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var currentVelocity = 0f

    /**
     * A [SpringAnimation] for this RecyclerView item. This animation rotates the view with a bouncy
     * spring configuration, resulting in the oscillation effect.
     *
     * The animation is started in [RecyclerView.addOnScrollListener].
     */
    val rotation: SpringAnimation = SpringAnimation(itemView, SpringAnimation.ROTATION)
        .setSpring(
            SpringForce()
                .setFinalPosition(0f)
                .setDampingRatio(AppearancePreferences.getScrollBounce())
                .setStiffness(stiffnessValue)
        )
        .addUpdateListener { _, _, velocity ->
            currentVelocity = velocity
        }

    /**
     * A [SpringAnimation] for this RecyclerView item. This animation is used to bring the item back
     * after the over-scroll effect.
     */
    val translationY: SpringAnimation = SpringAnimation(itemView, SpringAnimation.TRANSLATION_Y)
        .setSpring(
            SpringForce()
                .setFinalPosition(0f)
                .setDampingRatio(AppearancePreferences.getScrollBounce())
                .setStiffness(stiffnessValue)
        )

    companion object {
        val bouncyValue = AppearancePreferences.getScrollBounce()
        const val stiffnessValue = SpringForce.STIFFNESS_LOW
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
    }
}