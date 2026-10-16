package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.database.instances.TagsDatabase
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

            val untagged = allApps.filter { app ->
                !taggedPackages.contains(app.packageName)
            }.toCollection(ArrayList())

            untagged.sortBy { it.applicationInfo?.name ?: it.packageName }

            untaggedApps.postValue(untagged)
        }
    }
}
