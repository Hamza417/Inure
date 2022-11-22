package app.simple.inure.decorations.overscroll

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.preferences.BehaviourPreferences

open class HorizontalListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun getDrawable(resID: Int): Drawable {
        return ContextCompat.getDrawable(itemView.context, resID)!!
    }

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
                        .setDampingRatio(BehaviourPreferences.getDampingRatio())
                        .setStiffness(BehaviourPreferences.getStiffness())
            )
            .addUpdateListener { _, _, velocity ->
                currentVelocity = velocity
            }

    /**
     * A [SpringAnimation] for this RecyclerView item. This animation is used to bring the item back
     * after the over-scroll effect.
     */
    val translationX: SpringAnimation = SpringAnimation(itemView, SpringAnimation.TRANSLATION_X)
            .setSpring(
                    SpringForce()
                        .setFinalPosition(0f)
                        .setDampingRatio(BehaviourPreferences.getDampingRatio())
                        .setStiffness(BehaviourPreferences.getStiffness())
            )
}