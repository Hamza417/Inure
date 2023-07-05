package app.simple.inure.activities.app

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.FrameLayout
import android.widget.Toast
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.constants.IntentConstants
import app.simple.inure.constants.ShortcutConstants
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.crash.CrashReporter
import app.simple.inure.decorations.theme.ThemeCoordinatorLayout
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.preferences.*
import app.simple.inure.terminal.Term
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.ui.launcher.SplashScreen
import app.simple.inure.ui.panels.*
import app.simple.inure.ui.viewers.AudioPlayerPager
import app.simple.inure.util.ActivityUtils.getTopFragment
import app.simple.inure.util.AppUtils
import app.simple.inure.util.CalendarUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.Logger
import app.simple.inure.util.NullSafety.isNull
import com.topjohnwu.superuser.ipc.RootService
import java.time.ZonedDateTime
import java.util.*

class MainActivity : BaseActivity() {

    private lateinit var container: ThemeCoordinatorLayout
    private lateinit var content: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AndroidBug5497Workaround.assistActivity(this)
        ThemeManager.addListener(this)

        container = findViewById(R.id.app_container)
        content = findViewById(android.R.id.content)

        content.setBackgroundColor(ThemeManager.theme.viewGroupTheme.background)
        ThemeUtils.setAppTheme(resources)

        if (AppUtils.isBetaFlavor()) {
            setExpiryStamp()
        }

        if (savedInstanceState.isNull()) {
            if (MainPreferences.getLaunchCount().isZero()) {
                TrialPreferences.setFirstLaunchDate(System.currentTimeMillis())
            }

            MainPreferences.incrementLaunchCount()
            Log.d("MainActivity", "Launch count: ${MainPreferences.getLaunchCount()}")

            when (intent.action) {
                ShortcutConstants.ANALYTICS_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, Analytics.newInstance(), "analytics")
                        .addToBackStack("analytics")
                        .commit()
                }
                ShortcutConstants.APPS_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, Apps.newInstance(loading = true), "apps")
                        .addToBackStack("apps")
                        .commit()
                }
                ShortcutConstants.BATCH_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, Batch.newInstance(loading = true), "batch")
                        .addToBackStack("batch")
                        .commit()
                }
                ShortcutConstants.MOST_USED_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, MostUsed.newInstance(loader = true), "most_used")
                        .addToBackStack("most_used")
                        .commit()
                }
                ShortcutConstants.NOTES_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, Notes.newInstance(), "notes")
                        .addToBackStack("notes")
                        .commit()
                }
                ShortcutConstants.RECENTLY_INSTALLED_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, RecentlyInstalled.newInstance(loading = true), "recently_installed")
                        .addToBackStack("recently_installed")
                        .commit()
                }
                ShortcutConstants.RECENTLY_UPDATED_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, RecentlyUpdated.newInstance(loading = true), "recently_updated")
                        .addToBackStack("recently_updated")
                        .commit()
                }
                ShortcutConstants.TERMINAL_ACTION -> {
                    openHome()
                    startActivity(Intent(this, Term::class.java))
                    finish()
                }
                ShortcutConstants.UNINSTALLED_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, Uninstalled.newInstance(), "uninstalled")
                        .addToBackStack("uninstalled")
                        .commit()
                }
                ShortcutConstants.USAGE_STATS_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, Statistics.newInstance(loading = true), "stats")
                        .addToBackStack("stats")
                        .commit()
                }
                ShortcutConstants.PREFERENCES_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, Preferences.newInstance(), "preferences")
                        .addToBackStack("preferences")
                        .commit()
                }
                ShortcutConstants.SEARCH_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, Search.newInstance(true), "search")
                        .addToBackStack("search")
                        .commit()
                }
                ShortcutConstants.MUSIC_ACTION -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, Music.newInstance(), "music")
                        .addToBackStack("music")
                        .commit()
                }
                ShortcutConstants.AUDIO_PLAYER_ACTION -> {
                    if (supportFragmentManager.findFragmentByTag("audio_player_pager") == null) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_container,
                                     AudioPlayerPager.newInstance(
                                             MusicPreferences.getMusicPosition(),
                                             MusicPreferences.getFromSearch()), "audio_player_pager")
                            .commit()
                    } else {
                        Log.d("MainActivity", "Music player already open")
                        // Remove the fragment if it already exists
                        supportFragmentManager.beginTransaction()
                            .remove(supportFragmentManager.findFragmentByTag("audio_player_pager")!!)
                            .commitNowAllowingStateLoss()

                        // Add the fragment again
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_container,
                                     AudioPlayerPager.newInstance(
                                             MusicPreferences.getMusicPosition(),
                                             MusicPreferences.getFromSearch()), "audio_player_pager")
                            .commit()
                    }
                }
                "open_device_info" -> {
                    openHome()
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.app_container, DeviceInfo.newInstance(), "device_info")
                        .addToBackStack("device_info")
                        .commit()
                }
                IntentConstants.ACTION_UNLOCK -> {
                    if (packageManager.isPackageInstalled(AppUtils.unlockerPackageName)) {
                        if (TrialPreferences.isFullVersion()) {
                            showWarning(R.string.full_version_already_activated, goBack = false)

                            supportFragmentManager.beginTransaction()
                                .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                                .commit()
                        } else {
                            if (TrialPreferences.setFullVersion(value = true)) {
                                showWarning(R.string.full_version_activated, goBack = false)
                                TrialPreferences.resetUnlockerWarningCount()

                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                                    .commit()
                            } else {
                                showWarning(R.string.failed_to_activate_full_version, goBack = false)

                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                                    .commit()
                            }
                        }
                    } else {
                        showWarning(Warnings.gtUnknownAppStateWarning(), goBack = false)

                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                            .commit()
                    }
                }
                else -> {
                    if (AppUtils.isBetaFlavor()) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                            .commit()
                    } else {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                            .commit()
                    }
                }
            }
        }
    }

    private fun openHome() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            .replace(R.id.app_container, Home.newInstance(), "home")
            .commit()

        supportFragmentManager.executePendingTransactions()
    }

    private fun setExpiryStamp() {
        val expiryDate = Calendar.getInstance()

        expiryDate.clear()
        expiryDate.set(2023, Calendar.JULY, 6)
        expiryDate.timeZone = TimeZone.getTimeZone(ZonedDateTime.now().zone.id)

        if (CalendarUtils.isToday(expiryDate)) {
            Toast.makeText(applicationContext, "Application Expired!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("MainActivity", "onConfigurationChanged")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (AppearancePreferences.getTheme() == ThemeConstants.MATERIAL_YOU_DARK ||
                AppearancePreferences.getTheme() == ThemeConstants.MATERIAL_YOU_LIGHT) {
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
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            DevelopmentPreferences.crashHandler -> {
                if (DevelopmentPreferences.get(DevelopmentPreferences.crashHandler).invert()) {
                    CrashReporter(applicationContext).initialize()
                }
            }
        }
    }

    /**
     * Useless test although a nice one ;)
     *
     * Won't work!!!!!!!!
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_FORWARD -> {
                    Log.d("Inure", "KEYCODE_FORWARD")
                    println(getTopFragment()?.javaClass?.simpleName)
                    if (getTopFragment() is Home) {
                        Log.d("Inure", "KEYCODE_FORWARD: Home")
                        //                        supportFragmentManager.beginTransaction()
                        //                            .replace(R.id.app_container, Apps.newInstance(loading = true), "apps")
                        //                            .commit()
                    }

                    return true
                }
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeManager.removeListener(this)

        try {
            Logger.postVerboseLog("MainActivity destroyed")
            RootService.stop(Intent(this, RootService::class.java))
            Logger.postVerboseLog("RootService stopped")
        } catch (e: IllegalStateException) {
            Logger.postErrorLog(e.message ?: "RootService not running")
        }
    }
}