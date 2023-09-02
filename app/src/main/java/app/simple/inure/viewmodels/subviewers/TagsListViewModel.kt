package app.simple.inure.viewmodels.subviewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.database.instances.TagsDatabase
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TagsListViewModel(application: Application, private val tag: String) : PackageUtilsViewModel(application) {

    private val taggedApps: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    fun getTaggedApps(): LiveData<ArrayList<PackageInfo>> {
        return taggedApps
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        loadTaggedApps()
    }

    private fun loadTaggedApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = ArrayList<PackageInfo>()
            val tag = TagsDatabase.getInstance(application.applicationContext)?.getTagDao()?.getTag(tag)
            val allApps = getInstalledApps() + getUninstalledApps()

            tag?.packages?.split(",")?.forEach {
                allApps.stream().filter { app ->
                    app.packageName == it
                }.forEach { app ->
                    apps.add(app)
                }
            }

            taggedApps.postValue(apps)
        }
    }

    fun deleteTaggedApp(position: String, packageInfo: String, function: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val tag = TagsDatabase.getInstance(application.applicationContext)?.getTagDao()?.getTag(position)
            val packages = tag?.packages?.split(",")?.toMutableList()
            packages?.remove(packageInfo)
            tag?.packages = packages?.joinToString(",")
            TagsDatabase.getInstance(application.applicationContext)?.getTagDao()?.updateTag(tag!!)

            withContext(Dispatchers.Main) {
                function()
            }
        }
    }
}