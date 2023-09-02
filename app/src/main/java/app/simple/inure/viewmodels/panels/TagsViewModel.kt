package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.database.instances.TagsDatabase
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.models.Tag
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ConditionUtils.invert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TagsViewModel(application: Application) : PackageUtilsViewModel(application) {

    private val tags: MutableLiveData<ArrayList<Tag>> by lazy {
        MutableLiveData<ArrayList<Tag>>()
    }

    private val tagNames: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>().apply {
            loadTagNames()
        }
    }

    fun getTags(): LiveData<ArrayList<Tag>> {
        return tags
    }

    fun getTagNames(): LiveData<ArrayList<String>> {
        return tagNames
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            val tags = database?.getTagDao()?.getTags()
            val apps = getInstalledApps() + getUninstalledApps()

            this@TagsViewModel.tags.postValue(tags?.toArrayList()?.filter {
                it.packages.isNotEmpty() && it.packages.split(",").any { packageName ->
                    apps.any { app ->
                        app.packageName == packageName
                    }
                }
            }?.toArrayList() ?: ArrayList())
        }
    }

    private fun loadTagNames() {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            val tags = database?.getTagDao()?.getTagsNameOnly()
            tagNames.postValue(tags?.toArrayList() ?: ArrayList())
        }
    }

    fun addTag(tag: String, packageInfo: PackageInfo, function: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            val tags = database?.getTagDao()?.getTagsNameOnly()

            if (tags.isNullOrEmpty().invert()) {
                if (tags!!.contains(tag)) {
                    database.getTagDao()!!.getTag(tag).apply {
                        packages = if (packages.isNullOrEmpty()) {
                            packageInfo.packageName
                        } else {
                            packages.plus("," + packageInfo.packageName)
                        }

                        database.getTagDao()!!.updateTag(this)
                    }
                } else {
                    database.getTagDao()!!.insertTag(Tag(tag, packageInfo.packageName, -1))
                }
            } else {
                database?.getTagDao()!!.insertTag(Tag(tag, packageInfo.packageName, -1))
            }

            withContext(Dispatchers.Main) {
                function()
                loadTags()
                loadTagNames()
            }
        }
    }

    fun removeTag(tag: String, packageInfo: PackageInfo, function: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            val tags = database?.getTagDao()?.getTagsByPackage(packageInfo.packageName)?.toSet()

            if (tags.isNullOrEmpty().invert()) {
                if (tags!!.contains(tag)) {
                    database.getTagDao()!!.getTag(tag).apply {
                        packages = if (packages.contains("," + packageInfo.packageName)) {
                            packages.replace("," + packageInfo.packageName, "")
                        } else {
                            packages.replace(packageInfo.packageName, "")
                        }

                        database.getTagDao()!!.updateTag(this)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                function()
                loadTags()
                loadTagNames()
            }
        }
    }

    fun refresh() {
        refreshPackageData()
    }

    fun deleteTag(tag: Tag, function: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            database?.getTagDao()?.deleteTag(tag)
            tags.value?.remove(tag)
            withContext(Dispatchers.Main) {
                function()
                loadTagNames()
            }
        }
    }

    fun addMultipleAppsToTag(currentAppsList: java.util.ArrayList<BatchPackageInfo>, it: String, function: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            val tags = database?.getTagDao()?.getTagsNameOnly()

            if (tags.isNullOrEmpty().invert()) {
                if (tags!!.contains(it)) {
                    database.getTagDao()!!.getTag(it).apply {
                        packages = packages.plus("," + currentAppsList.joinToString(",") {
                            it.packageInfo.packageName
                        })

                        // Remove duplicates
                        packages = packages.split(",").distinct().joinToString(",")

                        database.getTagDao()!!.updateTag(this)
                    }
                } else {
                    database.getTagDao()!!.insertTag(Tag(it, currentAppsList.joinToString(",") {
                        it.packageInfo.packageName
                    }, -1))
                }
            } else {
                database?.getTagDao()!!.insertTag(Tag(it, currentAppsList.joinToString(",") {
                    it.packageInfo.packageName
                }, -1))
            }

            withContext(Dispatchers.Main) {
                loadTags()
                loadTagNames()
                function()
            }
        }
    }
}
