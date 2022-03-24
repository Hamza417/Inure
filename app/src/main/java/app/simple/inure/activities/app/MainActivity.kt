package app.simple.inure.activities.app

import android.animation.Animator
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import app.simple.inure.R
import app.simple.inure.decorations.theme.ThemeCoordinatorLayout
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.themes.interfaces.ThemeRevealCoordinatesListener
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.ui.launcher.SplashScreen
import app.simple.inure.util.CalendarUtils
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ThemeUtils
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.hypot

class MainActivity : BaseActivity(), ThemeRevealCoordinatesListener {

    private lateinit var circularRevealImageView: ImageView
    private lateinit var container: ThemeCoordinatorLayout
    private lateinit var content: FrameLayout
    private lateinit var tap: ImageView

    private var animator: Animator? = null
    private var xPoint = 0
    private var yPoint = 0
    private var isTapEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AndroidBug5497Workaround.assistActivity(this)
        ThemeManager.addListener(this)

        circularRevealImageView = findViewById(R.id.theme_reveal)
        container = findViewById(R.id.app_container)
        content = findViewById(android.R.id.content)
        tap = findViewById(R.id.tap)

        content.setBackgroundColor(ThemeManager.theme.viewGroupTheme.background)
        xPoint = container.measuredWidth / 2
        yPoint = container.measuredHeight / 2
        isTapEnabled = AccessibilityPreferences.isTapsShowing()

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                .commit()
        }
    }

    private fun setExpiryStamp() {
        val expiryDate = Calendar.getInstance()

        expiryDate.clear()
        expiryDate.set(2021, Calendar.DECEMBER, 31)
        expiryDate.timeZone = TimeZone.getTimeZone(ZonedDateTime.now().zone.id)

        if (CalendarUtils.isToday(expiryDate)) {
            Toast.makeText(applicationContext, "Application Expired!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ThemeUtils.setAppTheme(resources)
        ThemeUtils.setBarColors(resources, window)
    }

    private fun setTheme(theme: Theme, animate: Boolean = true) {
        if (!animate) {
            ThemeManager.theme = theme
            return
        }

        if (circularRevealImageView.isVisible) {
            return
        }

        val w = container.measuredWidth
        val h = container.measuredHeight

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        container.draw(canvas)

        circularRevealImageView.setImageBitmap(bitmap)
        circularRevealImageView.isVisible = true

        val finalRadius = hypot(w.toFloat(), h.toFloat())

        ThemeManager.theme = theme

        animator = ViewAnimationUtils
            .createCircularReveal(circularRevealImageView,
                                  xPoint,
                                  yPoint,
                                  finalRadius,
                                  0f)

        animator!!.duration = resources.getInteger(R.integer.theme_change_duration).toLong()
        animator!!.interpolator = DecelerateInterpolator(1.5F)

        animator!!.doOnEnd {
            circularRevealImageView.setImageDrawable(null)
            circularRevealImageView.isVisible = false
        }

        animator!!.start()
    }

    override fun onThemeChanged(theme: Theme) {
        setTheme(theme)
        ThemeUtils.setBarColors(resources, window)
        content.setBackgroundColor(ThemeManager.theme.viewGroupTheme.background)
    }

    override fun onTouchCoordinates(x: Float, y: Float) {
        xPoint = x.toInt()
        yPoint = y.toInt()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (isTapEnabled) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    with(tap) {
                        tap.clearAnimation()
                        x = event.x - width.div(2)
                        y = event.y - height.div(2)
                        visible(true)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    with(tap) {
                        x = event.x - width.div(2)
                        y = event.y - height.div(2)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    tap.clearAnimation()
                    tap.animate()
                        .scaleY(0.7F)
                        .scaleX(0.7F)
                        .alpha(0F)
                        .setStartDelay(200L)
                        .setInterpolator(DecelerateInterpolator())
                        .setDuration(250L)
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator?) {
                                /* no-op */
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                tap.visibility = View.INVISIBLE
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                                /* no-op */
                            }

                            override fun onAnimationRepeat(animation: Animator?) {
                                /* no-op */
                            }
                        })
                        .start()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AccessibilityPreferences.showTaps -> {
                isTapEnabled = AccessibilityPreferences.isTapsShowing()
                if (!isTapEnabled) tap.gone()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeManager.removeListener(this)
        animator?.cancel()
    }
}