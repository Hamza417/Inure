package app.simple.inure.decorations.transitions

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionValues

class CustomTransition : Transition() {
    override fun captureStartValues(transitionValues: TransitionValues) {
        transitionValues.values["custom:transition:alpha"] = transitionValues.view.alpha
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        transitionValues.values["custom:transition:alpha"] = 1f
    }

    override fun createAnimator(
            sceneRoot: ViewGroup,
            startValues: TransitionValues?,
            endValues: TransitionValues?
    ): Animator? {
        val view = endValues?.view ?: return null
        val startAlpha = startValues?.values?.get("custom:transition:alpha") as? Float ?: 0f
        val endAlpha = endValues.values["custom:transition:alpha"] as Float
        return ObjectAnimator.ofFloat(view, View.ALPHA, startAlpha, endAlpha)
    }
}
