package app.simple.inure.activities.app

import android.content.Intent
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
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.constants.ShortcutConstants
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.crash.CrashReport
import app.simple.inure.decorations.theme.ThemeCoordinatorLayout
import app.simple.inure.dialogs.app.License.Companion.showLicense
import app.simple.inure.dialogs.batch.BatchExtract.Companion.showBatchExtract
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.terminal.Term
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.ui.launcher.SplashScreen
import app.simple.inure.ui.panels.Analytics
import app.simple.inure.ui.panels.Apps
import app.simple.inure.ui.panels.Batch
import app.simple.inure.ui.panels.Debloat
import app.simple.inure.ui.panels.DeviceInfo
import app.simple.inure.ui.panels.FOSS
import app.simple.inure.ui.panels.Home
import app.simple.inure.ui.panels.MostUsed
import app.simple.inure.ui.panels.Music
import app.simple.inure.ui.panels.Notes
import app.simple.inure.ui.panels.Preferences
import app.simple.inure.ui.panels.RecentlyInstalled
import app.simple.inure.ui.panels.RecentlyUpdated
import app.simple.inure.ui.panels.Search
import app.simple.inure.ui.panels.Statistics
import app.simple.inure.ui.panels.Tags
import app.simple.inure.ui.panels.Uninstalled
import app.simple.inure.ui.subpanels.TaggedApps
import app.simple.inure.ui.viewers.AudioPlayer
import app.simple.inure.util.ActivityUtils.getTopFragment
import app.simple.inure.util.AppUtils
import app.simple.inure.util.AppUtils.isNewerUnlocker
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.viewmodels.launcher.LauncherViewModel
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.TimeZone

class MainActivity : BaseActivity() {

    private lateinit var container: ThemeCoordinatorLayout
    private lateinit var content: FrameLayout

    private val launcherViewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AndroidBug5497Workaround.assistActivity(this)
        ThemeManager.addListener(this)

        container = findViewById(R.id.app_container)
        content = findViewById(android.R.id.content)

        content.setBackgroundColor(ThemeManager.theme.viewGroupTheme.background)
        ThemeUtils.setAppTheme(resources)

        if (savedInstanceState.isNull()) {
            MainPreferences.incrementLaunchCount()
            openPanel(intent, isNewIntent = false)
        } else {
            Log.d("MainActivity", "savedInstanceState not null")
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launcherViewModel.initCheck()
            }
        }

        launcherViewModel.getShouldVerify().observe(this@MainActivity) { it ->
            if (it) {
                if (applicationContext.isNewerUnlocker()) {
                    supportFragmentManager.showLicense()
                } else {
                    if (TrialPreferences.isFullVersion().invert()) {
                        kotlin.runCatching {
                            if (TrialPreferences.setFullVersion(value = true)) {
                                showWarning(R.string.full_version_activated, goBack = false)
                            }
                        }.getOrElse {
                            it.printStackTrace()
                        }
                    }
                }
            } else {
                Log.i("License", "Verification not required")
            }
        }

        launcherViewModel.getWarning().observe(this@MainActivity) {
            showWarning(Warnings.getInvalidUnlockerWarning(), goBack = false)
            TrialPreferences.setFullVersion(false)
        }
    }

    override fun onNewIntent(intent: Intent) {
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

            ShortcutConstants.TAGS_ACTION -> {
                openHome(isNewIntent)
                openFragment(Tags.newInstance(), "tags")
            }

            ShortcutConstants.TAGGED_APPS_ACTION -> {
                openHome(isNewIntent)
                try {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.app_container, TaggedApps.newInstance(
                                intent.getStringExtra(ShortcutConstants.TAGGED_APPS_EXTRA)!!), "tagged_apps")
                        .addToBackStack("tagged_apps")
                        .commit()
                } catch (e: NullPointerException) {
                    showWarning("ERR: invalid tag constraint definition found", goBack = true)
                }
            }

            ShortcutConstants.FOSS_ACTION -> {
                openHome(isNewIntent)
                openFragment(FOSS.newInstance(), "foss")
            }

            ShortcutConstants.DEBLOAT_ACTION -> {
                openHome(isNewIntent)
                openFragment(Debloat.newInstance(), "debloat")
            }

            ShortcutConstants.MUSIC_ACTION -> {
                openHome(isNewIntent)
                openFragment(Music.newInstance(), "music")
            }

            ShortcutConstants.AUDIO_PLAYER_ACTION -> {
                openHome(isNewIntent)
                openFragment(AudioPlayer.newInstance(MusicPreferences.getMusicPosition()), "audio_player_pager")
            }

            "open_device_info" -> {
                openHome(isNewIntent)
                openFragment(DeviceInfo.newInstance(), "device_info")
            }

            ShortcutConstants.BATCH_EXTRACT_ACTION -> {
                openHome(isNewIntent)
                supportFragmentManager.showBatchExtract()
            }

            else -> {
                /**
                 * We don't want to open the splash screen if the app was opened from launcher
                 * and the app is already running in background.
                 */
                if (isNewIntent.invert()) { // Maybe the app was opened from launcher, need more checks?
                    if (AppUtils.isDebug()) {
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

    @Suppress("unused")
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
                    CrashReport(applicationContext).initialize()
                }
            }

            ConfigurationPreferences.language -> {
                recreate() // update the language in context wrapper
            }

            TrialPreferences.HAS_LICENSE_KEY -> {
                if (TrialPreferences.isFullVersion()) {
                    if (TrialPreferences.isUnlockerVerificationRequired().invert()) {
                        showWarning(R.string.full_version_activated, goBack = false)
                    } else {
                        showWarning(R.string.unlocker_not_installed, goBack = false)
                    }
                }
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
                /**
                 * If keycode is any letter, then it's a search query
                 * Open search panel
                 */
                in KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z -> {
                    supportFragmentManager.fragments.forEach {
                        if (it is Home) {
                            if (it.isVisible) {
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.app_container, Search.newInstance(firstLaunch = true), "search")
                                    .addToBackStack("search")
                                    .commit()
                            }
                        }
                    }

                    return super.onKeyDown(keyCode, event)
                }
                KeyEvent.KEYCODE_ESCAPE -> {
                    supportFragmentManager.fragments.forEach {
                        if (it is Search) {
                            if (it.isVisible) {
                                supportFragmentManager.popBackStack()
                            }
                        }
                    }

                    return super.onKeyDown(keyCode, event)
                }
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeManager.removeListener(this)

        try {
            RootService.stop(Intent(this, RootService::class.java))
            Log.d("RootService", "Stopped")
        } catch (e: IllegalStateException) {
            Log.d("RootService", "Not running")
        }
    }
}
