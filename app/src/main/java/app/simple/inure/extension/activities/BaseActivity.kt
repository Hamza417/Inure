package app.simple.inure.extension.activities

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.transition.Fade
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import app.simple.inure.R
import app.simple.inure.decorations.transitions.compat.DetailsTransitionArc
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.preferences.ShellPreferences.getHomePath
import app.simple.inure.preferences.ShellPreferences.setHomePath
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ThemeUtils
import app.simple.inure.util.ThemeUtils.setTheme

open class BaseActivity : AppCompatActivity(), ThemeChangedListener, android.content.SharedPreferences.OnSharedPreferenceChangeListener {

    override fun attachBaseContext(newBase: Context) {
        SharedPreferences.init(newBase)
        SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            sharedElementEnterTransition = DetailsTransitionArc()
            sharedElementReturnTransition = DetailsTransitionArc()
            enterTransition = Fade()
            exitTransition = Fade()
        }

        setTheme()
        setContentView(R.layout.activity_main)

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                                   .detectLeakedClosableObjects()
                                   .penaltyLog()
                                   .build())

        /**
         * Sets window flags for keeping the screen on
         */
        if (ConfigurationPreferences.isKeepScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if (!AppearancePreferences.isTransparentStatusDisabled()) {
            makeAppFullScreen()
            fixNavigationBarOverlap()
        }

        ThemeUtils.setAppTheme(resources)
        ThemeUtils.setBarColors(resources, window)
        setNavColor()

        // Terminal home path
        val defValue = getDir("HOME", MODE_PRIVATE).absolutePath
        val homePath = getHomePath(defValue)
        setHomePath(homePath!!)
    }

    private fun makeAppFullScreen() {
        if (AppearancePreferences.isTransparentStatusDisabled()) {
            window.statusBarColor = ThemeManager.theme.viewGroupTheme.background
            WindowCompat.setDecorFitsSystemWindows(window, true)
        } else {
            window.statusBarColor = Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = Color.TRANSPARENT
        }
    }

    private fun fixNavigationBarOverlap() {
        /**
         * Making the Navigation system bar not overlapping with the activity
         */
        if (Build.VERSION.SDK_INT >= 30) {
            /**
             * Root ViewGroup of this activity
             */
            val root = findViewById<CoordinatorLayout>(R.id.app_container)

            if (AppearancePreferences.isTransparentStatusDisabled()) {
                root.layoutParams = (root.layoutParams as FrameLayout.LayoutParams).apply {
                    leftMargin = 0
                    bottomMargin = 0
                    rightMargin = 0
                }

                root.requestLayout()
            } else {
                ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
                    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                    /**
                     * Apply the insets as a margin to the view. Here the system is setting
                     * only the bottom, left, and right dimensions, but apply whichever insets are
                     * appropriate to your layout. You can also update the view padding
                     * if that's more appropriate.
                     */
                    view.layoutParams = (view.layoutParams as FrameLayout.LayoutParams).apply {
                        leftMargin = insets.left
                        bottomMargin = insets.bottom
                        rightMargin = insets.right
                    }

                    /**
                     * Return CONSUMED if you don't want want the window insets to keep being
                     * passed down to descendant views.
                     */
                    WindowInsetsCompat.CONSUMED
                }
            }
        }
    }

    private fun setNavColor() {
        if (AppearancePreferences.isAccentOnNavigationBar()) {
            window.navigationBarColor = theme.obtainStyledAttributes(intArrayOf(R.attr.colorAppAccent)).getColor(0, 0)
        } else {
            window.navigationBarColor = Color.TRANSPARENT
        }

        ThemeUtils.updateNavAndStatusColors(resources, window)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.transparentStatus -> {
                makeAppFullScreen()
                fixNavigationBarOverlap()
            }
            AppearancePreferences.accentOnNav -> {
                setNavColor()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SharedPreferences.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }
}