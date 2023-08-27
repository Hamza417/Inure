package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.database.instances.TagsDatabase
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.Tag
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ConditionUtils.invert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TagsViewModel(application: Application) : PackageUtilsViewModel(application) {

    private val tags: MutableLiveData<ArrayList<Tag>> by lazy {
        MutableLiveData<ArrayList<Tag>>().apply {
            loadTags()
        }
    }

    fun getTags(): LiveData<ArrayList<Tag>> {
        return tags
    }

    private fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            val tags = database?.getTagDao()?.getTags()
            this@TagsViewModel.tags.postValue(tags?.toArrayList() ?: ArrayList())
        }
    }

    fun addTag(tag: String, packageInfo: PackageInfo, function: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            val tags = database?.getTagDao()?.getTagsNameOnly()

            if (tags.isNullOrEmpty().invert()) {
                if (tags!!.contains(tag)) {
                    database.getTagDao()!!.getTag(tag).apply {
                        packages = packages.plus("," + packageInfo.packageName)
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
            }
        }
    }
}
