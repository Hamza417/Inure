package app.simple.inure.decorations.overscroll

import androidx.dynamicanimation.animation.SpringForce

object RecyclerViewConstants {
    const val bouncyValue = SpringForce.DAMPING_RATIO_NO_BOUNCY
    const val stiffnessValue = SpringForce.STIFFNESS_LOW
    const val TYPE_HEADER = 0
    const val TYPE_ITEM = 1
    const val TYPE_DIVIDER = 2

    private const val value = 1.0f
    const val flingTranslationMagnitude = value
    const val overScrollRotationMagnitude = value
    const val overScrollTranslationMagnitude = value
}