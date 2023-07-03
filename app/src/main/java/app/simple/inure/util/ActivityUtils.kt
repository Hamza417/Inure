package app.simple.inure.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ComponentInfo
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.util.NullSafety.isNotNull

object ActivityUtils {
    /**
     * Launch a given activity class
     */
    @Throws
    fun launchPackage(context: Context, packageName: String, packageId: String) {
        val launchIntent = Intent(Intent.ACTION_MAIN)
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        launchIntent.component = ComponentName(packageName, packageId)
        startActivity(context, launchIntent, null)
    }

    @Suppress("DEPRECATION")
    fun createShortcut(context: Context, activityInfoModel: ActivityInfoModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val builder = ShortcutInfo.Builder(context, activityInfoModel.name.substringAfterLast("."))
                .setShortLabel(activityInfoModel.name.substringAfterLast("."))
                .setIcon(Icon.createWithBitmap(activityInfoModel.activityInfo.loadIcon(context.packageManager).toBitmap()))
                .setIntent(Intent(Intent.ACTION_MAIN)
                               .addCategory(Intent.CATEGORY_LAUNCHER)
                               .setComponent(ComponentName(activityInfoModel.activityInfo.packageName, activityInfoModel.name)))

            val shortcutInfo = builder.build()
            val shortcutManager = context.getSystemService(AppCompatActivity.SHORTCUT_SERVICE) as android.content.pm.ShortcutManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                shortcutManager.requestPinShortcut(shortcutInfo, null)
            } else {
                shortcutManager.addDynamicShortcuts(listOf(shortcutInfo))
            }
        } else {
            val shortcutIntent = Intent(Intent.ACTION_MAIN)
            shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            shortcutIntent.component = ComponentName(activityInfoModel.activityInfo.packageName, activityInfoModel.name)

            val intent = Intent()
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, activityInfoModel.name.substringAfterLast("."))
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, activityInfoModel.activityInfo.loadIcon(context.packageManager).toBitmap())
            intent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
            context.sendBroadcast(intent)
        }
    }

    /**
     * Launch a given activity class with [Intent.setAction]
     */
    fun launchPackage(context: Context, packageName: String, packageId: String, action: String) {
        val launchIntent = Intent(Intent.ACTION_MAIN)
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        launchIntent.action = action
        launchIntent.component = ComponentName(packageName, packageId)
        startActivity(context, launchIntent, null)
    }

    @kotlin.jvm.Throws(java.lang.IllegalArgumentException::class)
    fun isEnabled(context: Context, packageName: String, clsName: String): Boolean {
        val componentName = ComponentName(packageName, clsName)

        return when (context.packageManager.getComponentEnabledSetting(componentName)) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ->       // We need to get the application info to get the component's default state
                try {
                    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            context.packageManager
                                .getPackageInfo(packageName,
                                                PackageManager.PackageInfoFlags.of(
                                                        (PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS).toLong()))
                        } else {
                            @Suppress("DEPRECATION")
                            context.packageManager
                                .getPackageInfo(packageName,
                                                PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS)
                        }
                    } else {
                        @Suppress("deprecation")
                        context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES
                                or PackageManager.GET_DISABLED_COMPONENTS)
                    }

                    val components: ArrayList<ComponentInfo> = ArrayList()

                    if (packageInfo.activities.isNotNull()) {
                        for (i in packageInfo.activities) {
                            components.add(i)
                        }
                    }

                    for (componentInfo in components) {
                        if (componentInfo.name == clsName) {
                            return componentInfo.isEnabled
                        }
                    }

                    // the component is not declared in the AndroidManifest
                    false
                } catch (e: PackageManager.NameNotFoundException) {
                    // the package isn't installed on the device
                    false
                }

            else -> {
                try {
                    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            context.packageManager
                                .getPackageInfo(packageName,
                                                PackageManager.PackageInfoFlags.of(
                                                        (PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS).toLong()))
                        } else {
                            @Suppress("DEPRECATION")
                            context.packageManager
                                .getPackageInfo(packageName,
                                                PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS)
                        }
                    } else {
                        @Suppress("deprecation")
                        context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES
                                or PackageManager.GET_DISABLED_COMPONENTS)
                    }

                    val components: ArrayList<ComponentInfo> = ArrayList()

                    if (packageInfo.activities.isNotNull()) {
                        for (i in packageInfo.activities) {
                            components.add(i)
                        }
                    }

                    for (componentInfo in components) {
                        if (componentInfo.name == clsName) {
                            return componentInfo.isEnabled
                        }
                    }

                    // the component is not declared in the AndroidManifest
                    false
                } catch (e: PackageManager.NameNotFoundException) {
                    // the package isn't installed on the device
                    false
                }
            }
        }
    }

    fun AppCompatActivity.getTopFragment(): Fragment? {
        supportFragmentManager.run {
            return when (backStackEntryCount) {
                0 -> null
                else -> findFragmentByTag(getBackStackEntryAt(backStackEntryCount - 1).name)
            }
        }
    }
}
