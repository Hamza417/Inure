package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.FOSSParser
import app.simple.inure.apk.utils.PackageUtils
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

            apps = filterAppsByType(apps)
            apps = filterAppsByCategory(apps)
            val filteredList = filterAppsByFlags(apps)

            filteredList.getSortedList(AppsPreferences.getSortStyle(), AppsPreferences.isReverseSorting())
            appData.postValue(filteredList)
        }
    }

    private fun filterAppsByType(apps: ArrayList<PackageInfo>): ArrayList<PackageInfo> {
        return when (AppsPreferences.getAppsType()) {
            SortConstant.SYSTEM -> {
                apps.parallelStream().filter { packageInfo ->
                    packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
            SortConstant.USER -> {
                apps.parallelStream().filter { packageInfo ->
                    packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
            else -> apps
        }
    }

    private fun filterAppsByCategory(apps: ArrayList<PackageInfo>): ArrayList<PackageInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return apps

        val categoryList = ArrayList<PackageInfo>()
        for (app in apps) {
            when (app.safeApplicationInfo.category) {
                ApplicationInfo.CATEGORY_UNDEFINED -> addAppIfFlagSet(categoryList, app, SortConstant.CATEGORY_UNSPECIFIED)
                ApplicationInfo.CATEGORY_GAME -> addAppIfFlagSet(categoryList, app, SortConstant.CATEGORY_GAME)
                ApplicationInfo.CATEGORY_AUDIO -> addAppIfFlagSet(categoryList, app, SortConstant.CATEGORY_AUDIO)
                ApplicationInfo.CATEGORY_VIDEO -> addAppIfFlagSet(categoryList, app, SortConstant.CATEGORY_VIDEO)
                ApplicationInfo.CATEGORY_IMAGE -> addAppIfFlagSet(categoryList, app, SortConstant.CATEGORY_IMAGE)
                ApplicationInfo.CATEGORY_SOCIAL -> addAppIfFlagSet(categoryList, app, SortConstant.CATEGORY_SOCIAL)
                ApplicationInfo.CATEGORY_NEWS -> addAppIfFlagSet(categoryList, app, SortConstant.CATEGORY_NEWS)
                ApplicationInfo.CATEGORY_MAPS -> addAppIfFlagSet(categoryList, app, SortConstant.CATEGORY_MAPS)
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> addAppIfFlagSet(categoryList, app, SortConstant.CATEGORY_PRODUCTIVITY)
                ApplicationInfo.CATEGORY_ACCESSIBILITY -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        addAppIfFlagSet(categoryList, app, SortConstant.CATEGORY_ACCESSIBILITY)
                    }
                }
            }
        }
        return categoryList
    }

    private fun addAppIfFlagSet(categoryList: ArrayList<PackageInfo>, app: PackageInfo, flag: Long) {
        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), flag) && !categoryList.contains(app)) {
            categoryList.add(app)
        }
    }

    private fun filterAppsByFlags(apps: ArrayList<PackageInfo>): ArrayList<PackageInfo> {
        val filteredList = arrayListOf<PackageInfo>()
        for (packageInfo in apps) {
            if (shouldAddApp(packageInfo)) {
                filteredList.add(packageInfo)
            }
        }
        return filteredList.stream().distinct().collect(Collectors.toList()) as ArrayList<PackageInfo>
    }

    private fun shouldAddApp(packageInfo: PackageInfo): Boolean {
        return when {
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.UNINSTALLED) -> {
                packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0
            }
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.SPLIT) -> {
                packageInfo.safeApplicationInfo.splitSourceDirs?.isNotEmpty() == true
            }
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.DISABLED) -> {
                !packageInfo.safeApplicationInfo.enabled &&
                        packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0
            }
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.APK) -> {
                packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()
            }
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.ENABLED) -> {
                packageInfo.safeApplicationInfo.enabled &&
                        packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0
            }
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.FOSS) -> {
                FOSSParser.isPackageFOSS(packageInfo)
            }
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.LARGE_HEAP) -> {
                packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_LARGE_HEAP != 0
            }
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.LAUNCHABLE) -> {
                PackageUtils.isAppLaunchable(applicationContext(), packageInfo.packageName)
            }
            FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.STOPPED) -> {
                packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_STOPPED != 0
            }
            else -> {
                false
            }
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
