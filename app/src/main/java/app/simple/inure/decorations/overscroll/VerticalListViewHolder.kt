package app.simple.inure.decorations.overscroll

import android.content.Context
import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.preferences.BehaviourPreferences

open class VerticalListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val context: Context = itemView.context

    fun getString(resId: Int): String {
        return context.getString(resId)
    }

    /**
     * A [SpringAnimation] for this RecyclerView item. This animation is used to bring the item back
     * after the over-scroll effect.
     */
    val translationY: SpringAnimation = SpringAnimation(itemView, SpringAnimation.TRANSLATION_Y)
        .setSpring(
                SpringForce()
                    .setFinalPosition(0f)
                    .setDampingRatio(BehaviourPreferences.getDampingRatio())
                    .setStiffness(BehaviourPreferences.getStiffness()))
}