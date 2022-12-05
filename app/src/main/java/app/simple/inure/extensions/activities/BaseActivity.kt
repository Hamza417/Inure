package app.simple.inure.extensions.activities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.transition.ArcMotion
import android.transition.Fade
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.database.instances.StackTraceDatabase
import app.simple.inure.decorations.transitions.compat.DetailsTransitionArc
import app.simple.inure.dialogs.miscellaneous.Error.Companion.showError
import app.simple.inure.dialogs.miscellaneous.Warning.Companion.showWarning
import app.simple.inure.popups.behavior.PopupArcType
import app.simple.inure.popups.behavior.PopupTransitionType
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.preferences.ShellPreferences.getHomePath
import app.simple.inure.preferences.ShellPreferences.setHomePath
import app.simple.inure.themes.data.MaterialYou
import app.simple.inure.themes.data.MaterialYou.presetMaterialYouDynamicColors
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ContextUtils
import app.simple.inure.util.LocaleHelper
import app.simple.inure.util.ThemeUtils
import app.simple.inure.util.ThemeUtils.setTheme
import com.google.android.material.transition.platform.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity(), ThemeChangedListener, android.content.SharedPreferences.OnSharedPreferenceChangeListener {

    override fun attachBaseContext(newBaseContext: Context) {
        SharedPreferences.init(newBaseContext)
        SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
        super.attachBaseContext(ContextUtils.updateLocale(newBaseContext, ConfigurationPreferences.getAppLanguage()!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            presetMaterialYouDynamicColors()

            if (AppearancePreferences.isMaterialYouAccent()) {
                AppearancePreferences.setAccentColor(ContextCompat.getColor(baseContext, MaterialYou.materialYouAccentResID))
            }
        }

        ThemeUtils.setAppTheme(resources)

        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            setBackgroundDrawable(ColorDrawable(ThemeManager.theme.viewGroupTheme.background))
            sharedElementsUseOverlay = true
            sharedElementEnterTransition = DetailsTransitionArc()
            sharedElementReturnTransition = DetailsTransitionArc()
            exitTransition = Fade()
            enterTransition = Fade()
            reenterTransition = Fade()
        }

        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setTheme()
        setContentView(R.layout.activity_main)

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                                   .detectAll()
                                   .build())

        /**
         * Sets window flags for keeping the screen on
         */
        if (ConfigurationPreferences.isKeepScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if (!AppearancePreferences.isTransparentStatusDisabled()) {
            makeAppFullScreen()
        }

        fixNavigationBarOverlap()

        /**
         * Keeps the instance of current locale of the app
         */
        LocaleHelper.setAppLocale(ConfigurationCompat.getLocales(resources.configuration)[0]!!)

        ThemeUtils.setBarColors(resources, window)
        setNavColor()

        // Terminal home path
        val defValue = getDir("HOME", MODE_PRIVATE).absolutePath
        val homePath = getHomePath(defValue)
        setHomePath(homePath!!)
    }

    @Suppress("unused")
    private fun setTransitions() {
        with(window) {
            if (BehaviourPreferences.isTransitionOn()) {
                when (BehaviourPreferences.getTransitionType()) {
                    PopupTransitionType.FADE -> {
                        exitTransition = Fade()
                        enterTransition = Fade()
                        reenterTransition = Fade()
                    }
                    PopupTransitionType.ELEVATION -> {
                        exitTransition = MaterialElevationScale(false)
                        enterTransition = MaterialElevationScale(true)
                        reenterTransition = MaterialElevationScale(false)
                    }
                    PopupTransitionType.SHARED_AXIS_X -> {
                        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
                        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
                    }
                    PopupTransitionType.SHARED_AXIS_Y -> {
                        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
                        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
                    }
                    PopupTransitionType.SHARED_AXIS_Z -> {
                        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                    }
                    PopupTransitionType.THROUGH -> {
                        exitTransition = MaterialFadeThrough()
                        enterTransition = MaterialFadeThrough()
                        reenterTransition = MaterialFadeThrough()
                    }
                }
            }
        }
    }

    @Suppress("unused")
    private fun setArc() {
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        with(window) {
            if (BehaviourPreferences.isArcAnimationOn()) {
                // TODO - Fix activity crashing on [MaterialContainerTransform]
                when (PopupArcType.LEGACY /* BehaviourPreferences.getArcType() */) {
                    PopupArcType.INURE -> {
                        sharedElementEnterTransition = MaterialContainerTransform().apply {
                            duration = resources.getInteger(R.integer.animation_duration).toLong()
                            setAllContainerColors(Color.TRANSPARENT)
                            scrimColor = Color.TRANSPARENT
                            addTarget(android.R.id.content)
                            pathMotion = ArcMotion().apply {
                                maximumAngle = this.maximumAngle
                                minimumHorizontalAngle = this.minimumHorizontalAngle
                                minimumVerticalAngle = this.minimumVerticalAngle
                            }
                        }
                        sharedElementReturnTransition = MaterialContainerTransform().apply {
                            duration = resources.getInteger(R.integer.animation_duration).toLong()
                            setAllContainerColors(Color.TRANSPARENT)
                            scrimColor = Color.TRANSPARENT
                            addTarget(android.R.id.content)
                            pathMotion = ArcMotion().apply {
                                maximumAngle = this.maximumAngle
                                minimumHorizontalAngle = this.minimumHorizontalAngle
                                minimumVerticalAngle = this.minimumVerticalAngle
                            }
                        }
                    }
                    PopupArcType.MATERIAL -> {
                        sharedElementEnterTransition = MaterialContainerTransform().apply {
                            duration = resources.getInteger(R.integer.animation_duration).toLong()
                            setAllContainerColors(Color.TRANSPARENT)
                            scrimColor = Color.TRANSPARENT
                            addTarget(android.R.id.content)
                            pathMotion = MaterialArcMotion()
                        }
                        sharedElementReturnTransition = MaterialContainerTransform().apply {
                            duration = resources.getInteger(R.integer.animation_duration).toLong()
                            setAllContainerColors(Color.TRANSPARENT)
                            scrimColor = Color.TRANSPARENT
                            addTarget(android.R.id.content)
                            pathMotion = MaterialArcMotion()
                        }
                    }
                    PopupArcType.LEGACY -> {
                        sharedElementEnterTransition = DetailsTransitionArc()
                        sharedElementReturnTransition = DetailsTransitionArc()
                    }
                }
            }
        }
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

    /**
     * Making the Navigation system bar not overlapping with the activity
     */
    fun fixNavigationBarOverlap() {
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
                // val imeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
                // val imeHeight = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom

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

    private fun setNavColor() {
        if (AppearancePreferences.isAccentOnNavigationBar()) {
            window.navigationBarColor = AppearancePreferences.getAccentColor()
        } else {
            window.navigationBarColor = Color.TRANSPARENT
        }

        ThemeUtils.updateNavAndStatusColors(resources, window)
    }

    protected fun showError(error: String) {
        try {
            supportFragmentManager.showError(error).setOnErrorCallbackListener {
                onBackPressedDispatcher.onBackPressed()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    open fun showWarning(warning: String, goBack: Boolean = true) {
        supportFragmentManager.showWarning(warning).setOnWarningCallbackListener {
            if (goBack) {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    open fun showWarning(@StringRes warning: Int, goBack: Boolean = true) {
        supportFragmentManager.showWarning(warning).setOnWarningCallbackListener {
            if (goBack) {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.transparentStatus -> {
                makeAppFullScreen()
                fixNavigationBarOverlap()
            }
            AppearancePreferences.accentColor,
            AppearancePreferences.accentOnNav -> {
                setNavColor()
            }
            BehaviourPreferences.arcType -> {
                // setArc()
            }
            BehaviourPreferences.transitionType -> {
                // setTransitions()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        /**
         * Create a global database instance maybe?
         */
        lifecycleScope.launchWhenCreated {
            withContext(Dispatchers.IO) {
                StackTraceDatabase.init(applicationContext)
            }
        }
    }
}