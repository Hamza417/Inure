package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.FOSSParser
import app.simple.inure.apk.utils.PackageUtils.isAppLargeHeap
import app.simple.inure.apk.utils.PackageUtils.isAppLaunchable
import app.simple.inure.apk.utils.PackageUtils.isAppStopped
import app.simple.inure.apk.utils.PackageUtils.isInstalled
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.viewmodels.DataGeneratorViewModel
import app.simple.inure.preferences.AppsPreferences
import app.simple.inure.util.ArrayUtils.toArrayList
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
        val filter = AppsPreferences.getAppsFilter()
        val appInfo = packageInfo.safeApplicationInfo

        val conditions = listOf(
                Pair(FlagUtils.isFlagSet(filter, SortConstant.UNINSTALLED), !packageInfo.isInstalled()),
                Pair(FlagUtils.isFlagSet(filter, SortConstant.SPLIT), !appInfo.splitSourceDirs.isNullOrEmpty()),
                Pair(FlagUtils.isFlagSet(filter, SortConstant.DISABLED), !appInfo.enabled && packageInfo.isInstalled()),
                Pair(FlagUtils.isFlagSet(filter, SortConstant.APK), appInfo.splitSourceDirs.isNullOrEmpty()),
                Pair(FlagUtils.isFlagSet(filter, SortConstant.ENABLED), appInfo.enabled && packageInfo.isInstalled()),
                Pair(FlagUtils.isFlagSet(filter, SortConstant.FOSS), FOSSParser.isPackageFOSS(packageInfo)),
                Pair(FlagUtils.isFlagSet(filter, SortConstant.LARGE_HEAP), packageInfo.isAppLargeHeap()),
                Pair(FlagUtils.isFlagSet(filter, SortConstant.LAUNCHABLE), packageInfo.isAppLaunchable(applicationContext())),
                Pair(FlagUtils.isFlagSet(filter, SortConstant.STOPPED), packageInfo.isAppStopped())
        )

        return if (AppsPreferences.isFilterStyleAnd()) {
            conditions.filter { it.first }.all { it.second }
        } else {
            conditions.any { it.first && it.second }
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
