package app.simple.inure.apk.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.ComponentInfo
import android.content.pm.PackageManager
import android.os.Build
import app.simple.inure.util.NullSafety.isNotNull

object ServicesUtils {
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
                                                        (PackageManager.GET_SERVICES or PackageManager.MATCH_DISABLED_COMPONENTS).toLong()))
                        } else {
                            @Suppress("DEPRECATION")
                            context.packageManager
                                .getPackageInfo(packageName,
                                                PackageManager.GET_SERVICES or PackageManager.MATCH_DISABLED_COMPONENTS)
                        }
                    } else {
                        @Suppress("deprecation")
                        context.packageManager.getPackageInfo(packageName, PackageManager.GET_SERVICES
                                or PackageManager.GET_DISABLED_COMPONENTS)
                    }

                    val components: ArrayList<ComponentInfo> = ArrayList()

                    if (packageInfo.services.isNotNull()) {
                        for (i in packageInfo.services) {
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
                                                        (PackageManager.GET_SERVICES or PackageManager.MATCH_DISABLED_COMPONENTS).toLong()))
                        } else {
                            @Suppress("DEPRECATION")
                            context.packageManager
                                .getPackageInfo(packageName,
                                                PackageManager.GET_SERVICES or PackageManager.MATCH_DISABLED_COMPONENTS)
                        }
                    } else {
                        @Suppress("deprecation")
                        context.packageManager.getPackageInfo(packageName, PackageManager.GET_SERVICES
                                or PackageManager.GET_DISABLED_COMPONENTS)
                    }

                    val components: ArrayList<ComponentInfo> = ArrayList()

                    if (packageInfo.services.isNotNull()) {
                        for (i in packageInfo.services) {
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