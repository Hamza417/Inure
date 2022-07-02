package app.simple.inure.decorations.overscroll

import app.simple.inure.preferences.BehaviourPreferences

object RecyclerViewConstants {
    var bouncyValue = BehaviourPreferences.getDampingRatio()
    var stiffnessValue = BehaviourPreferences.getStiffness()

    const val TYPE_HEADER = 0
    const val TYPE_ITEM = 1
    const val TYPE_DIVIDER = 2

    private const val value = 1.0f
    const val flingTranslationMagnitude = value
    const val overScrollRotationMagnitude = value
    const val overScrollTranslationMagnitude = value
}