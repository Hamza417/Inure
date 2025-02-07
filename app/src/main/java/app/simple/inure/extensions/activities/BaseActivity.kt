package app.simple.inure.extensions.activities

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.transition.ArcMotion
import android.transition.Fade
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.BuildCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.PredictiveBackControl
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.simple.inure.R
import app.simple.inure.database.instances.StackTraceDatabase
import app.simple.inure.decorations.transitions.compat.DetailsTransitionArc
import app.simple.inure.dialogs.app.FullVersion.Companion.showFullVersion
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.dialogs.miscellaneous.Error.Companion.showError
import app.simple.inure.dialogs.miscellaneous.Loader
import app.simple.inure.dialogs.miscellaneous.Warning.Companion.showWarning
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.popups.behavior.PopupArcType
import app.simple.inure.popups.behavior.PopupTransitionType
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.preferences.SharedPreferences.registerEncryptedSharedPreferencesListener
import app.simple.inure.preferences.SharedPreferences.registerSharedPreferencesListener
import app.simple.inure.preferences.SharedPreferences.unregisterEncryptedSharedPreferencesListener
import app.simple.inure.preferences.SharedPreferences.unregisterListener
import app.simple.inure.preferences.ShellPreferences
import app.simple.inure.preferences.ShellPreferences.getHomePath
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.themes.data.MaterialYou
import app.simple.inure.themes.data.MaterialYou.presetMaterialYouDynamicColors
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.themes.manager.ThemeUtils.setTheme
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ContextUtils
import app.simple.inure.util.LocaleUtils
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.SDCard
import app.simple.inure.util.ViewUtils.defaultPadding
import app.simple.inure.util.ViewUtils.setPaddingLeft
import app.simple.inure.util.ViewUtils.setPaddingRight
import com.google.android.material.transition.platform.MaterialArcMotion
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.android.material.transition.platform.MaterialElevationScale
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsposed.hiddenapibypass.HiddenApiBypass

@SuppressLint("Registered") // This activity should not be registered in the manifest
open class BaseActivity : AppCompatActivity(),
                          ThemeChangedListener,
                          android.content.SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Fragments own loader instance
     */
    private var loader: Loader? = null

    private var cutoutDepth = 0
    private var requestCode = 0x283D

    override fun attachBaseContext(newBaseContext: Context) {
        SharedPreferences.init(newBaseContext)
        SharedPreferences.initEncrypted(newBaseContext)
        registerSharedPreferencesListener(this)
        registerEncryptedSharedPreferencesListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("L")
        }
        super.attachBaseContext(ContextUtils.updateLocale(newBaseContext, ConfigurationPreferences.getAppLanguage()!!))
    }

    @OptIn(BuildCompat.PrereleaseSdkCheck::class, PredictiveBackControl::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AppearancePreferences.migrateMaterialYouTheme()
            presetMaterialYouDynamicColors()

            if (AppearancePreferences.isMaterialYouAccent()) {
                AppearancePreferences.setAccentColor(ContextCompat.getColor(baseContext, MaterialYou.MATERIAL_YOU_ACCENT_RES_ID))
            }
        }

        // Disable splash screen
        ThemeUtils.setAppTheme(baseContext.resources)
        TrialPreferences.migrateLegacy()

        // File(Environment.getExternalStorageDirectory().absolutePath + "/" + ConfigurationPreferences.getAppPath()).deleteRecursively()

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

        // Disable predictive back for fragments
        FragmentManager.enablePredictiveBack(DevelopmentPreferences.get(DevelopmentPreferences.TEST_PREDICTIVE_BACK_GESTURE))
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        //        /**
        //         * Workaround for webview dark page issue
        //         */
        //        if(ThemeUtils.isFollowSystem()) {
        //            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        //        } else {
        //            if(ThemeUtils.isNightMode(resources)) {
        //                Log.d("BaseActivity", "Night mode enabled")
        //                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        //            } else {
        //                Log.d("BaseActivity", "Night mode disabled")
        //                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //            }
        //        }

        AppearancePreferences.maxIconSize = resources.getDimensionPixelSize(R.dimen.app_icon_dimension) / 4
        try {
            TrialPreferences.setFirstLaunchDate(packageManager.getPackageInfo(packageName, 0).firstInstallTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }

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

        if (!DevelopmentPreferences.get(DevelopmentPreferences.DISABLE_TRANSPARENT_STATUS)) {
            makeAppFullScreen()
        }

        fixNavigationBarOverlap()
        enableNotchArea()

        /**
         * Keeps the instance of current locale of the app
         */
        LocaleUtils.setAppLocale(ConfigurationCompat.getLocales(resources.configuration)[0]!!)

        ThemeUtils.setBarColors(resources, window)
        setNavColor()
        applyInsets()

        // Terminal home path
        val defValue = getDir("HOME", MODE_PRIVATE).absolutePath
        val homePath = getHomePath(defValue)
        ShellPreferences.setHomePath(homePath!!)

        /**
         * Create a global database instance maybe?
         */
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                withContext(Dispatchers.IO) {
                    StackTraceDatabase.init(applicationContext)

                    if (SDCard.findSdCardPath(applicationContext).isNull()) {
                        ApkBrowserPreferences.setExternalStorage(false)
                        ConfigurationPreferences.setExternalStorage(false)

                        Log.d("BaseActivity", "No external storage found")
                    } else {
                        Log.d("BaseActivity", "External storage found")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (DevelopmentPreferences.get(DevelopmentPreferences.IS_NOTCH_AREA_ENABLED)) {
            if (orientationListener.canDetectOrientation()) {
                orientationListener.enable()
            }
        }

        if (ThemeUtils.isDayNight()) {
            ThemeUtils.setAppTheme(resources)
        }
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

    @Suppress("unused", "KotlinConstantConditions")
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

    @Suppress("DEPRECATION")
    private fun makeAppFullScreen() {
        if (DevelopmentPreferences.get(DevelopmentPreferences.DISABLE_TRANSPARENT_STATUS)) {
            window.statusBarColor = ThemeManager.theme.viewGroupTheme.background
            WindowCompat.setDecorFitsSystemWindows(window, true)
        } else {
            window.statusBarColor = Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (DevelopmentPreferences.get(DevelopmentPreferences.DIVIDER_ON_NAVIGATION_BAR)) {
                window.navigationBarDividerColor = Color.TRANSPARENT
            } else {
                window.navigationBarDividerColor = ThemeManager.theme.viewGroupTheme.dividerBackground
            }
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

        if (DevelopmentPreferences.get(DevelopmentPreferences.DISABLE_TRANSPARENT_STATUS)) {
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

    @Suppress("DEPRECATION")
    private fun setNavColor(accent: Boolean = false) {
        val startColor: Int
        val endColor: Int
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        if (accent) {
            if (AppearancePreferences.isAccentOnNavigationBar()) {
                startColor = window.navigationBarColor
                endColor = AppearancePreferences.getAccentColor()
            } else {
                startColor = ThemeManager.theme.viewGroupTheme.background
                endColor = ThemeManager.theme.viewGroupTheme.background
            }
        } else {
            if (AppearancePreferences.isAccentOnNavigationBar()) {
                startColor = ThemeManager.theme.viewGroupTheme.background
                endColor = AppearancePreferences.getAccentColor()
            } else {
                startColor = AppearancePreferences.getAccentColor()
                endColor = ThemeManager.theme.viewGroupTheme.background
            }
        }

        // Animate color
        val valueAnimator = ValueAnimator.ofArgb(/* ...values = */ startColor, endColor)
        valueAnimator.duration = resources.getInteger(R.integer.animation_duration).toLong()
        valueAnimator.addUpdateListener { animation ->
            window.navigationBarColor = animation.animatedValue as Int
        }
        valueAnimator.start()

        ThemeUtils.updateNavAndStatusColors(resources, window)
    }

    private fun enableNotchArea() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (DevelopmentPreferences.get(DevelopmentPreferences.IS_NOTCH_AREA_ENABLED)) {
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            } else {
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            }

            /**
             * If the notch area is enabled, we need to add padding to the left side of the app
             * to avoid the notch area overlapping the app content and the app content overlapping
             * the notch area.
             */
            if (DevelopmentPreferences.get(DevelopmentPreferences.IS_NOTCH_AREA_ENABLED)) {
                val root = findViewById<ViewGroup>(R.id.app_container)

                ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                    cutoutDepth = insets.top
                    WindowInsetsCompat.CONSUMED
                }
            }
        }
    }

    private val orientationListener by lazy {
        object : OrientationEventListener(applicationContext, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                try {
                    if (DevelopmentPreferences.get(DevelopmentPreferences.IS_NOTCH_AREA_ENABLED).invert()) {
                        return
                    }

                    if (orientation == ORIENTATION_UNKNOWN) {
                        return
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        with(findViewById<ViewGroup>(R.id.app_container)) {
                            when (display?.rotation) {
                                Surface.ROTATION_0 -> {
                                    // Bottom - reset the padding in portrait
                                    defaultPadding()
                                }

                                Surface.ROTATION_90 -> {
                                    // Left
                                    setPaddingLeft(cutoutDepth)
                                }

                                Surface.ROTATION_180 -> {
                                    // Top - reset the padding if upside down
                                    defaultPadding()
                                }

                                Surface.ROTATION_270 -> {
                                    // Right
                                    setPaddingRight(cutoutDepth)
                                }
                            }
                        }
                    } else {
                        with(findViewById<ViewGroup>(R.id.app_container)) {
                            @Suppress("DEPRECATION")
                            when ((getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation) {
                                Surface.ROTATION_0 -> {
                                    // Bottom - reset the padding in portrait
                                    defaultPadding()
                                }

                                Surface.ROTATION_90 -> {
                                    // Left
                                    setPaddingLeft(cutoutDepth)
                                }

                                Surface.ROTATION_180 -> {
                                    // Top - reset the padding if upside down
                                    defaultPadding()
                                }

                                Surface.ROTATION_270 -> {
                                    // Right
                                    setPaddingRight(cutoutDepth)
                                }
                            }
                        }
                    }
                } catch (_: NullPointerException) {
                    /* no-op */
                }
            }
        }
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

    open fun fullVersionCheck(): Boolean {
        return if (TrialPreferences.isAppFullVersionEnabled()) {
            true
        } else {
            supportFragmentManager.showFullVersion().setFullVersionCallbacks {
                onBackPressedDispatcher.onBackPressed()
            }
            false
        }
    }

    open fun fullVersionCheck(function: () -> Unit): Boolean {
        return if (TrialPreferences.isAppFullVersionEnabled()) {
            true
        } else {
            supportFragmentManager.showFullVersion().setFullVersionCallbacks {
                function()
            }
            false
        }
    }

    open fun showLoader() {
        loader = Loader.newInstance()
        loader?.show(supportFragmentManager, "loader")
    }

    open fun hideLoader() {
        loader?.dismiss()
    }

    protected fun onSure(onSure: () -> Unit) {
        supportFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
            override fun onSure() {
                onSure()
            }
        })
    }

    private fun applyInsets() {
        lifecycleScope.launch {
            delay((0x2710..0x61A8).random().toLong())
            try {
                val method = TrialPreferences::class.java.getDeclaredMethod("getMaxDays")
                method.isAccessible = true

                // Check if the method is static
                val isStatic = java.lang.reflect.Modifier.isStatic(method.modifiers)
                val maxDays = if (isStatic) {
                    method.invoke(null) as Int
                } else {
                    val instance = TrialPreferences // Create an instance if the method is not static
                    method.invoke(instance) as Int
                }

                if (maxDays > 0xF) {
                    finish()
                }
            } catch (e: NoSuchMethodException) {
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                val buildConfigClass = Class.forName("app.simple.inure.BuildConfig")
                val versionCodeField = buildConfigClass.getDeclaredField("VERSION_CODE")
                versionCodeField.isAccessible = true
                val versionCode = versionCodeField.getInt(null)

                if (requestCode != versionCode) {
                    finish()
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        when (key) {
            DevelopmentPreferences.DISABLE_TRANSPARENT_STATUS,
            DevelopmentPreferences.DIVIDER_ON_NAVIGATION_BAR -> {
                makeAppFullScreen()
                fixNavigationBarOverlap()
            }

            AppearancePreferences.ACCENT_COLOR,
            AppearancePreferences.ACCENT_ON_NAV -> {
                Log.d("BaseActivity", "Accent color changed")
                setNavColor()
            }

            BehaviourPreferences.ARC_TYPE -> {
                // setArc()
            }

            BehaviourPreferences.TRANSITION_TYPE -> {
                // setTransitions()
            }

            DevelopmentPreferences.IS_NOTCH_AREA_ENABLED -> {
                enableNotchArea()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        orientationListener.disable()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterListener(this)
        unregisterEncryptedSharedPreferencesListener(this)
    }
}
