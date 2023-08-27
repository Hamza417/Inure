package app.simple.inure.viewmodels.panels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.database.instances.TagsDatabase
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.Tag
import app.simple.inure.util.ArrayUtils.toArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
}