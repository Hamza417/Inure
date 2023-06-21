package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.SortConstant
import app.simple.inure.events.AppsEvent
import app.simple.inure.extensions.viewmodels.DataGeneratorViewModel
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class AppsViewModel(application: Application) : DataGeneratorViewModel(application) {

    private val appData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            loadPackageData()
        }
    }

    val appLoaded: MutableLiveData<AppsEvent<Boolean>> by lazy {
        MutableLiveData<AppsEvent<Boolean>>()
    }

    fun getAppData(): LiveData<ArrayList<PackageInfo>> {
        return appData
    }

    fun isAppDataEmpty(): Boolean {
        return appData.value.isNullOrEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    fun loadAppData() {
        viewModelScope.launch(Dispatchers.Default) {
            var apps = getInstalledApps()

            when (MainPreferences.getAppsCategory()) {
                SortConstant.SYSTEM -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
                SortConstant.USER -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
            }

            var filteredList = arrayListOf<PackageInfo>()

            if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.COMBINE_FLAGS)) { // Pretty special case, even I don't know what I did here
                filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
                    if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                        if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                            true
                        } else {
                            p.applicationInfo.enabled.invert()
                        }
                    } else {
                        true
                    } && if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                        if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                            true
                        } else {
                            p.applicationInfo.enabled
                        }
                    } else {
                        true
                    } && if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.APK)) {
                        if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                            true
                        } else {
                            p.applicationInfo.splitSourceDirs.isNullOrEmpty()
                        }
                    } else {
                        true
                    } && if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                        if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.APK)) {
                            true
                        } else {
                            p.applicationInfo.splitSourceDirs?.isNotEmpty() ?: false
                        }
                    } else {
                        true
                    }
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
            } else {
                if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                    filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
                        p.applicationInfo.enabled.invert()
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
                }

                if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                    filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
                        p.applicationInfo.enabled
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
                }

                if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.APK)) {
                    filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
                        p.applicationInfo.splitSourceDirs.isNullOrEmpty()
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
                }

                if (FlagUtils.isFlagSet(MainPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                    filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
                        p.applicationInfo.splitSourceDirs?.isNotEmpty() ?: false
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
                }

                // Remove duplicate elements
                filteredList = filteredList.stream().distinct().collect(Collectors.toList()) as ArrayList<PackageInfo>
            }

            filteredList.getSortedList(MainPreferences.getSortStyle(), MainPreferences.isReverseSorting())

            appData.postValue(filteredList as ArrayList<PackageInfo>?)
            appLoaded.postValue(AppsEvent(true))
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