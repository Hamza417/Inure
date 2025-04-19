package app.simple.inure.extensions.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.transition.Fade
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.BuildCompat
import androidx.core.os.ConfigurationCompat
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
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.preferences.AppearancePreferences
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
import app.simple.inure.util.ContextUtils
import app.simple.inure.util.LocaleUtils
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.SDCard
import kotlinx.coroutines.Dispatchers
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
            setBackgroundDrawable(ThemeManager.theme.viewGroupTheme.background.toDrawable())
            sharedElementsUseOverlay = true
            sharedElementEnterTransition = DetailsTransitionArc()
            sharedElementReturnTransition = DetailsTransitionArc()
            exitTransition = Fade()
            enterTransition = Fade()
            reenterTransition = Fade()
        }

        enableEdgeToEdge()
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

        /**
         * Keeps the instance of current locale of the app
         */
        LocaleUtils.setAppLocale(ConfigurationCompat.getLocales(resources.configuration)[0]!!)
        ThemeUtils.setBarColors(resources, window)

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
        if (ThemeUtils.isDayNight()) {
            ThemeUtils.setAppTheme(resources)
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

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        when (key) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterListener(this)
        unregisterEncryptedSharedPreferencesListener(this)
    }
}
