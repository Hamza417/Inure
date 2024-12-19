package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.FOSSParser
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.viewmodels.DataGeneratorViewModel
import app.simple.inure.preferences.AppsPreferences
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class AppsViewModel(application: Application) : DataGeneratorViewModel(application) {

    private val appData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    fun getAppData(): LiveData<ArrayList<PackageInfo>> {
        return appData
    }

    fun isAppDataEmpty(): Boolean {
        return appData.value.isNullOrEmpty()
    }

    fun shouldShowLoader(): Boolean {
        return appData.value.isNullOrEmpty()
    }

    fun loadAppData() {
        viewModelScope.launch(Dispatchers.Default) {
            var apps = (getInstalledApps() + getUninstalledApps()).toArrayList()

            when (AppsPreferences.getAppsType()) {
                SortConstant.SYSTEM -> {
                    apps = apps.parallelStream().filter { packageInfo ->
                        packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
                SortConstant.USER -> {
                    apps = apps.parallelStream().filter { packageInfo ->
                        packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val categoryList = ArrayList<PackageInfo>()

                for (app in apps) {
                    if (app.safeApplicationInfo.category == ApplicationInfo.CATEGORY_UNDEFINED) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_UNSPECIFIED)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.safeApplicationInfo.category == ApplicationInfo.CATEGORY_GAME) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_GAME)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.safeApplicationInfo.category == ApplicationInfo.CATEGORY_AUDIO) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_AUDIO)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.safeApplicationInfo.category == ApplicationInfo.CATEGORY_VIDEO) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_VIDEO)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.safeApplicationInfo.category == ApplicationInfo.CATEGORY_IMAGE) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_IMAGE)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.safeApplicationInfo.category == ApplicationInfo.CATEGORY_SOCIAL) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_SOCIAL)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.safeApplicationInfo.category == ApplicationInfo.CATEGORY_NEWS) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_NEWS)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.safeApplicationInfo.category == ApplicationInfo.CATEGORY_MAPS) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_MAPS)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.safeApplicationInfo.category == ApplicationInfo.CATEGORY_PRODUCTIVITY) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_PRODUCTIVITY)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (app.safeApplicationInfo.category == ApplicationInfo.CATEGORY_ACCESSIBILITY) {
                            if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_ACCESSIBILITY)) {
                                if (!categoryList.contains(app)) {
                                    categoryList.add(app)
                                }
                            }
                        }
                    }
                }

                apps = categoryList //.stream().distinct().collect(Collectors.toList()) as ArrayList<PackageInfo> // Unnecessary ??
            }

            var filteredList = arrayListOf<PackageInfo>()

            for (packageInfo in apps) {
                if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
                    if (packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0) {
                        if (!filteredList.contains(packageInfo)) {
                            filteredList.add(packageInfo)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                    if (packageInfo.safeApplicationInfo.splitSourceDirs?.isNotEmpty() == true) {
                        if (!filteredList.contains(packageInfo)) {
                            filteredList.add(packageInfo)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                    if (!packageInfo.safeApplicationInfo.enabled &&
                            packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0) {
                        if (!filteredList.contains(packageInfo)) {
                            filteredList.add(packageInfo)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.APK)) {
                    if (packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                        if (!filteredList.contains(packageInfo)) {
                            filteredList.add(packageInfo)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                    if (packageInfo.safeApplicationInfo.enabled &&
                            packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0) {
                        if (!filteredList.contains(packageInfo)) {
                            filteredList.add(packageInfo)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.FOSS)) {
                    if (FOSSParser.isPackageFOSS(packageInfo)) {
                        if (!filteredList.contains(packageInfo)) {
                            filteredList.add(packageInfo)
                        }
                    }
                }
            }

            // Remove duplicate elements
            filteredList = filteredList.stream().distinct().collect(Collectors.toList()) as ArrayList<PackageInfo>
            filteredList.getSortedList(AppsPreferences.getSortStyle(), AppsPreferences.isReverseSorting())

            appData.postValue(filteredList as ArrayList<PackageInfo>?)
        }
    }

    @Suppress("unused")
    private fun checkCombinedFlags(packageInfo: PackageInfo): Boolean {
        return when {
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.DISABLED) -> {
                if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                    packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0
                } else {
                    packageInfo.safeApplicationInfo.enabled.invert() &&
                            packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0
                }
            }
            else -> {
                true
            }
        } && when {
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.ENABLED) -> {
                if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                    packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0
                } else {
                    packageInfo.safeApplicationInfo.enabled &&
                            packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0
                }
            }
            else -> {
                true
            }
        } && when {
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.APK) -> {
                if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                    true
                } else {
                    packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()
                }
            }
            else -> {
                true
            }
        } && when {
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.SPLIT) -> {
                if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.APK)) {
                    true
                } else {
                    packageInfo.safeApplicationInfo.splitSourceDirs?.isNotEmpty() ?: false
                }
            }
            else -> {
                true
            }
        } && when {
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.UNINSTALLED) -> {
                packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0
            }
            else -> {
                true
            }
        } && when {
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.FOSS) -> {
                FOSSParser.isPackageFOSS(packageInfo)
            }
            else -> {
                true
            }
        }
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        loadAppData()
    }

    override fun onAppUninstalled(packageName: String?) {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = appData.value

            if (apps != null) {
                for (i in apps.indices) {
                    if (apps[i].packageName == packageName) {
                        apps.removeAt(i)
                        break
                    }
                }

                appData.postValue(apps)
            }
        }
    }
}
