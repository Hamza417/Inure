package app.simple.inure.activities.app

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.terminal.Term
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.ui.launcher.SplashScreen
import app.simple.inure.ui.music.Music
import app.simple.inure.ui.panels.*
import app.simple.inure.util.AppUtils
import app.simple.inure.util.CalendarUtils
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ThemeUtils
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

        if (savedInstanceState.isNull()) {
            if (MainPreferences.getLaunchCount().isZero()) {
                MainPreferences.setFirstLaunchDate(System.currentTimeMillis())
            }

            MainPreferences.incrementLaunchCount()
            Log.d("MainActivity", "Launch count: ${MainPreferences.getLaunchCount()}")

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
                IntentConstants.ACTION_UNLOCK -> {
                    if (packageManager.isPackageInstalled(AppUtils.unlockerPackageName)) {
                        if (MainPreferences.isFullVersion()) {
                            showWarning(R.string.full_version_already_activated, goBack = false)

                            supportFragmentManager.beginTransaction()
                                .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                                .commit()
                        } else {
                            if (MainPreferences.setFullVersion(value = true)) {
                                showWarning(R.string.full_version_activated, goBack = false)

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
                        showWarning(Warnings.getInureWarning03(), goBack = false)

                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_container, SplashScreen.newInstance(false), "splash_screen")
                            .commit()
                    }
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
                if (DevelopmentPreferences.get(DevelopmentPreferences.crashHandler)) {
                    CrashReporter(applicationContext).initialize()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeManager.removeListener(this)
    }
}