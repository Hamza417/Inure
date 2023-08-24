package app.simple.inure.activities.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.constants.IntentConstants
import app.simple.inure.constants.Misc
import app.simple.inure.constants.ShortcutConstants
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.crash.CrashReporter
import app.simple.inure.decorations.theme.ThemeCoordinatorLayout
import app.simple.inure.dialogs.app.InstallUpdate.Companion.showInstallUpdate
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.services.DataLoaderService
import app.simple.inure.terminal.Term
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.ui.launcher.SplashScreen
import app.simple.inure.ui.panels.Analytics
import app.simple.inure.ui.panels.Apps
import app.simple.inure.ui.panels.Batch
import app.simple.inure.ui.panels.DeviceInfo
import app.simple.inure.ui.panels.Home
import app.simple.inure.ui.panels.MostUsed
import app.simple.inure.ui.panels.Music
import app.simple.inure.ui.panels.Notes
import app.simple.inure.ui.panels.Preferences
import app.simple.inure.ui.panels.RecentlyInstalled
import app.simple.inure.ui.panels.RecentlyUpdated
import app.simple.inure.ui.panels.Search
import app.simple.inure.ui.panels.Statistics
import app.simple.inure.ui.panels.Uninstalled
import app.simple.inure.ui.viewers.AudioPlayerPager
import app.simple.inure.util.ActivityUtils.getTopFragment
import app.simple.inure.util.AppUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.Logger
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ParcelUtils.serializable
import com.topjohnwu.superuser.ipc.RootService
import java.io.File
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.TimeZone

class MainActivity : BaseActivity() {

    private lateinit var container: ThemeCoordinatorLayout
    private lateinit var content: FrameLayout

    private lateinit var broadcastReceiver: BroadcastReceiver
    private val intentFilter = IntentFilter()

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
            MainPreferences.incrementLaunchCount()
            openPanel(intent, isNewIntent = false)
        } else {
            Log.d("MainActivity", "savedInstanceState not null")
        }

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == DataLoaderService.UPDATE_DOWNLOADED) {
                    if (intent.extras?.serializable<File>(DataLoaderService.UPDATE) != null) {
                        val update = intent.extras?.serializable<File>(DataLoaderService.UPDATE)
                        if (update?.exists() == true) {
                            try {
                                supportFragmentManager.showInstallUpdate(update).setOnInstallCallbackListener {
                                    val intent1 = Intent(Intent.ACTION_VIEW)
                                    intent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    intent1.setDataAndType(
                                            FileProvider.getUriForFile(
                                                    applicationContext,
                                                    applicationContext.packageName + ".provider",
                                                    update
                                            ), "application/vnd.android.package-archive"
                                    )
                                    startActivity(intent1)
                                }
                            } catch (e: java.lang.IllegalStateException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }

        intentFilter.addAction(DataLoaderService.UPDATE_DOWNLOADED)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        openPanel(intent, isNewIntent = true)
    }

    private fun openPanel(intent: Intent?, isNewIntent: Boolean = false) {
        when (intent?.action) {
            ShortcutConstants.ANALYTICS_ACTION -> {
                openHome(isNewIntent)
                openFragment(Analytics.newInstance(), "analytics")
            }

            ShortcutConstants.APPS_ACTION -> {
                openHome(isNewIntent)
                openFragment(Apps.newInstance(loading = true), "apps")
            }

            ShortcutConstants.BATCH_ACTION -> {
                openHome(isNewIntent)
                openFragment(Batch.newInstance(loading = true), "batch")
            }

            ShortcutConstants.MOST_USED_ACTION -> {
                openHome(isNewIntent)
                openFragment(MostUsed.newInstance(loader = true), "most_used")
            }

            ShortcutConstants.NOTES_ACTION -> {
                openHome(isNewIntent)
                openFragment(Notes.newInstance(), "notes")
            }

            ShortcutConstants.RECENTLY_INSTALLED_ACTION -> {
                openHome(isNewIntent)
                openFragment(RecentlyInstalled.newInstance(loading = true), "recently_installed")
            }

            ShortcutConstants.RECENTLY_UPDATED_ACTION -> {
                openHome(isNewIntent)
                openFragment(RecentlyUpdated.newInstance(loading = true), "recently_updated")
            }

            ShortcutConstants.TERMINAL_ACTION -> {
                openHome(isNewIntent)
                startActivity(Intent(this, Term::class.java))
                // finish() // should not finish
            }

            ShortcutConstants.UNINSTALLED_ACTION -> {
                openHome(isNewIntent)
                openFragment(Uninstalled.newInstance(), "uninstalled")
            }

            ShortcutConstants.USAGE_STATS_ACTION -> {
                openHome(isNewIntent)
                openFragment(Statistics.newInstance(loading = true), "statistics")
            }

            ShortcutConstants.PREFERENCES_ACTION -> {
                openHome(isNewIntent)
                openFragment(Preferences.newInstance(), "preferences")
            }

            ShortcutConstants.SEARCH_ACTION -> {
                openHome(isNewIntent)
                openFragment(Search.newInstance(firstLaunch = true), "search")
            }

            ShortcutConstants.MUSIC_ACTION -> {
                openHome(isNewIntent)
                openFragment(Music.newInstance(), "music")
            }

            ShortcutConstants.AUDIO_PLAYER_ACTION -> {
                openFragment(AudioPlayerPager.newInstance(MusicPreferences.getMusicPosition()), "audio_player_pager")
            }

            "open_device_info" -> {
                openHome(isNewIntent)
                openFragment(DeviceInfo.newInstance(), "device_info")
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
                /**
                 * We don't want to open the splash screen if the app was opened from launcher
                 * and the app is already running in background.
                 */
                if (isNewIntent.invert()) { // Maybe the app was opened from launcher, need more checks?
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

    private fun openHome(isNewIntent: Boolean) {
        if (isNewIntent.invert()) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.app_container, Home.newInstance(), "home")
                .commit()

            supportFragmentManager.executePendingTransactions()
        }
    }

    private fun openFragment(fragment: Fragment, tag: String) {
        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.app_container, fragment, tag)
                .addToBackStack(tag)
                .setReorderingAllowed(true)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .show(supportFragmentManager.findFragmentByTag(tag)!!)
                .setReorderingAllowed(true)
                .commit()
        }
    }

    private fun setExpiryStamp() {
        val expiryDate = Calendar.getInstance()
        val today = Calendar.getInstance()

        expiryDate.clear()
        expiryDate.set(2023, Calendar.AUGUST, 17)
        expiryDate.timeZone = TimeZone.getTimeZone(ZonedDateTime.now().zone.id)

        today.timeZone = TimeZone.getTimeZone(ZonedDateTime.now().zone.id)

        if (today.after(expiryDate) || today == expiryDate) {
            Toast.makeText(applicationContext, "Application Expired!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
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

            ConfigurationPreferences.language -> {
                recreate() // update the language in context wrapper
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        /**
         * Store the touch coordinates
         */
        if (ev.action == MotionEvent.ACTION_DOWN) {
            Misc.xOffset = ev.rawX
            Misc.yOffset = ev.rawY
        }
        return super.dispatchTouchEvent(ev)
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
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiver)

        try {
            RootService.stop(Intent(this, RootService::class.java))
            Logger.postVerboseLog("RootService stopped")
        } catch (e: IllegalStateException) {
            Logger.postErrorLog(e.message ?: "RootService not running")
        }
    }
}