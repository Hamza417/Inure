package app.simple.inure.decorations.overscroll

import android.content.Context
import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.decorations.overscroll.RecyclerViewConstants.bouncyValue
import app.simple.inure.decorations.overscroll.RecyclerViewConstants.stiffnessValue

open class VerticalListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val context: Context = itemView.context

    fun getString(resId: Int): String {
        return context.getString(resId)
    }

    private var currentVelocity = 0f

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
                        .setDampingRatio(bouncyValue)
                        .setStiffness(stiffnessValue))
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
                        .setDampingRatio(bouncyValue)
                        .setStiffness(stiffnessValue))

}