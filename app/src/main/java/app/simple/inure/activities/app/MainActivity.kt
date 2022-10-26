package app.simple.inure.activities.app

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import app.simple.inure.R
import app.simple.inure.constants.ShortcutConstants
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.crash.CrashReporter
import app.simple.inure.decorations.theme.ThemeCoordinatorLayout
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.interfaces.blur.BlurCallbackListener
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.terminal.Term
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.ui.app.Apps
import app.simple.inure.ui.launcher.SplashScreen
import app.simple.inure.ui.music.Music
import app.simple.inure.ui.panels.*
import app.simple.inure.util.CalendarUtils
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ThemeUtils
import java.time.ZonedDateTime
import java.util.*

class MainActivity : BaseActivity(), BlurCallbackListener {

    private lateinit var container: ThemeCoordinatorLayout
    private lateinit var content: FrameLayout
    private var valueAnimator: ValueAnimator? = null

    private val maxBlur = 10F
    private val minBlur = 0.1F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AndroidBug5497Workaround.assistActivity(this)
        ThemeManager.addListener(this)

        container = findViewById(R.id.app_container)
        content = findViewById(android.R.id.content)

        content.setBackgroundColor(ThemeManager.theme.viewGroupTheme.background)
        ThemeUtils.setAppTheme(resources)

        if (savedInstanceState.isNull()) {
            when (intent.action) {
                ShortcutConstants.ANALYTICS_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Analytics.newInstance(), "apps")
                        .commit()
                }
                ShortcutConstants.APPS_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Apps.newInstance(loading = true), "apps")
                        .commit()
                }
                ShortcutConstants.BATCH_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Batch.newInstance(loading = true), "batch")
                        .commit()
                }
                ShortcutConstants.MOST_USED_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, MostUsed.newInstance(loader = true), "most_used")
                        .commit()
                }
                ShortcutConstants.NOTES_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Notes.newInstance(), "notes")
                        .commit()
                }
                ShortcutConstants.RECENTLY_INSTALLED_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, RecentlyInstalled.newInstance(loading = true), "recently_installed")
                        .commit()
                }
                ShortcutConstants.RECENTLY_UPDATED_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, RecentlyUpdated.newInstance(loading = true), "recently_updated")
                        .commit()
                }
                ShortcutConstants.TERMINAL_ACTION -> {
                    startActivity(Intent(this, Term::class.java))
                    finish()
                }
                ShortcutConstants.UNINSTALLED_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Uninstalled.newInstance(), "uninstalled")
                        .commit()
                }
                ShortcutConstants.USAGE_STATS_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Statistics.newInstance(loading = true), "stats")
                        .commit()
                }
                ShortcutConstants.PREFERENCES_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Preferences.newInstance(), "preferences")
                        .commit()
                }
                ShortcutConstants.SEARCH_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Search.newInstance(true), "search")
                        .commit()
                }
                ShortcutConstants.MUSIC_ACTION -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, Music.newInstance(), "music")
                        .commit()
                }
                "open_device_info" -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, DeviceInformation.newInstance(), "device_info")
                        .commit()
                }
                else -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                        .commit()
                }
            }
        }
    }

    @Suppress("unused")
    private fun setExpiryStamp() {
        val expiryDate = Calendar.getInstance()

        expiryDate.clear()
        expiryDate.set(2022, Calendar.DECEMBER, 31)
        expiryDate.timeZone = TimeZone.getTimeZone(ZonedDateTime.now().zone.id)

        if (CalendarUtils.isToday(expiryDate)) {
            Toast.makeText(applicationContext, "Application Expired!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (AppearancePreferences.getTheme() == ThemeConstants.MATERIAL_YOU) {
                recreate()
            }
        }
        ThemeUtils.setAppTheme(resources)
        ThemeUtils.setBarColors(resources, window)
    }

    override fun onThemeChanged(theme: Theme, animate: Boolean) {
        ThemeUtils.setBarColors(resources, window)
        content.setBackgroundColor(ThemeManager.theme.viewGroupTheme.background)
        window.setBackgroundDrawable(ColorDrawable(ThemeManager.theme.viewGroupTheme.background))
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            DevelopmentPreferences.crashHandler -> {
                if (DevelopmentPreferences.isUsingNativeCrashHandler()) {
                    CrashReporter(applicationContext).initialize()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        valueAnimator?.cancel()
        ThemeManager.removeListener(this)
    }

    private fun blur(viewId: Int, from: Float = 1F, to: Float = 25F, interpolator: TimeInterpolator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            valueAnimator = ValueAnimator.ofFloat(from, to)
            valueAnimator?.duration = resources.getInteger(R.integer.animation_duration).toLong()
            valueAnimator?.interpolator = interpolator
            valueAnimator?.addUpdateListener { animation ->
                with(animation.animatedValue as Float) {
                    (findViewById<CoordinatorLayout>(viewId) as View)
                        .setRenderEffect(RenderEffect.createBlurEffect(this, this, Shader.TileMode.CLAMP))
                }
            }
            valueAnimator?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    if (to <= minBlur) {
                        (findViewById<CoordinatorLayout>(viewId) as View)
                            .setRenderEffect(null)
                    }
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }

            })
            valueAnimator?.start()
        }
    }

    override fun onWindowOpened() {
        blur(R.id.app_container, minBlur, maxBlur, LinearOutSlowInInterpolator())
    }

    override fun onWindowClosed() {
        blur(R.id.app_container, maxBlur, minBlur, LinearOutSlowInInterpolator())
    }
}