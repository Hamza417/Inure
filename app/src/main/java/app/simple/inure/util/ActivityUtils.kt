package app.simple.inure.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ComponentInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat.startActivity
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
}
