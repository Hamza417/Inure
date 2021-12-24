package app.simple.inure.util

import android.animation.Animator
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import app.simple.inure.R
import app.simple.inure.R.*
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.util.ColorUtils.resolveAttrColor

object ViewUtils {
    /**
     * Dim the background when PopupWindow shows
     * Should be called from showAsDropDown function
     * because this is when container's parent is
     * initialized
     */
    fun dimBehind(contentView: View) {
        if (BehaviourPreferences.isDimmingOn()) {
            val container = contentView.rootView
            val windowManager = contentView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val layoutParams = container.layoutParams as WindowManager.LayoutParams
            layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            layoutParams.dimAmount = getDimValue(contentView.context)
            windowManager.updateViewLayout(container, layoutParams)
        }
    }

    fun View.setMargins(marginLeft: Int, marginTop: Int, marginRight: Int, marginBottom: Int) {
        val params: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(marginLeft, marginTop, marginRight, marginBottom)
        this.layoutParams = params
    }

    /**
     * Get screen dim value based on interface theme
     */
    fun getDimValue(context: Context): Float {
        val outValue = TypedValue()
        context.resources.getValue(dimen.screen_dim, outValue, true)
        return outValue.float
    }

    /**
     * Adds outline shadows to the view using the accent color
     * of the app
     *
     * @param contentView [View] that needs to be elevated with colored
     *                    shadow
     */
    fun addShadow(contentView: View) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P && BehaviourPreferences.areShadowsOn()) {
            contentView.outlineAmbientShadowColor = contentView.context.resolveAttrColor(R.attr.colorAppAccent)
            contentView.outlineSpotShadowColor = contentView.context.resolveAttrColor(R.attr.colorAppAccent)
        }
    }

    /**
     * Makes the view go away
     */
    fun View.gone() {
        clearAnimation()
        this.visibility = View.GONE
    }

    /**
     * Makes the view go away
     *
     * @param animate adds animation to the process
     */
    fun View.invisible(animate: Boolean) {
        if (animate) {
            clearAnimation()
            this.animate()
                .scaleY(0F)
                .scaleX(0F)
                .alpha(0F)
                .setInterpolator(AccelerateInterpolator())
                .setDuration(this.resources.getInteger(R.integer.animation_duration).toLong())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                        /* no-op */
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        this@invisible.visibility = View.INVISIBLE
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        /* no-op */
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                        /* no-op */
                    }
                })
                .start()
        } else {
            this.visibility = View.INVISIBLE
        }
    }

    /**
     * Makes the view come back
     *
     * @param animate adds animation to the process
     */
    fun View.visible(animate: Boolean) {
        if (animate) {
            clearAnimation()
            this.animate()
                .scaleY(1F)
                .scaleX(1F)
                .alpha(1F)
                .setInterpolator(LinearOutSlowInInterpolator())
                .setDuration(this.resources.getInteger(R.integer.animation_duration).toLong())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                        this@visible.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        /* no-op */
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        /* no-op */
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                        /* no-op */
                    }
                })
                .start()
        } else {
            this.visibility = View.VISIBLE
        }
    }
}