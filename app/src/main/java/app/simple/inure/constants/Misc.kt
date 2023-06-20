package app.simple.inure.constants

import androidx.dynamicanimation.animation.SpringForce

object Misc {
    const val delay = 500L
    const val roundedCornerFactor = 1.5F
    const val maxBlur = 10F
    const val minBlur = 0.1F
    const val blurRadius = 16F
    const val dimAmount = 0.75F

    // Hover props
    const val hoverAnimationDuration = 250L
    const val hoverAnimationDampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
    const val hoverAnimationStiffness = SpringForce.STIFFNESS_LOW
    const val hoverAnimationScaleOnHover = 0.90F
    const val hoverAnimationScaleOnUnHover = 1.0F
    const val hoverAnimationElevation = 10F
    const val hoverAnimationAlpha = 0.8F

    // Misc
    const val SHIZUKU_CODE = 654

    const val splitApkFormat = ".apks"
    const val apkFormat = ".apk"
}