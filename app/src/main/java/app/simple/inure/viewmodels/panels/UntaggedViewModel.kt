package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.SortConstant
import app.simple.inure.database.instances.TagsDatabase
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.preferences.UntaggedPreferences
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class UntaggedViewModel(application: Application) : PackageUtilsViewModel(application) {

    private val untaggedApps: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    fun getUntaggedApps(): LiveData<ArrayList<PackageInfo>> {
        return untaggedApps
    }

    fun shouldShowLoader(): Boolean {
        return untaggedApps.value.isNullOrEmpty()
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        loadUntaggedApps()
    }

    fun loadUntaggedApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val allApps = getInstalledApps()
            val database = TagsDatabase.getInstance(applicationContext())
            val tags = database?.getTagDao()?.getTags()

            val taggedPackages = mutableSetOf<String>()
            tags?.forEach { tag ->
                tag.packages.split(",").forEach { packageName ->
                    if (packageName.isNotEmpty()) {
                        taggedPackages.add(packageName)
                    }
                }
            }

            var untagged = allApps.filter { app ->
                !taggedPackages.contains(app.packageName)
            }.toCollection(ArrayList())

            untagged = filterAppsByType(untagged)

            untagged.getSortedList(UntaggedPreferences.getSortStyle(), UntaggedPreferences.isReverseSorting())

            untaggedApps.postValue(untagged)
        }
    }

    private fun filterAppsByType(apps: ArrayList<PackageInfo>): ArrayList<PackageInfo> {
        return when (UntaggedPreferences.getAppsType()) {
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
}
