package app.simple.inure.viewmodels.activity

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.FileSizeHelper.getDirectorySize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ManageSpaceViewModel(application: Application) : WrappedViewModel(application) {

    private val imagesCachePath = "${applicationContext().cacheDir}/image_manager_disk_cache/"
    private val appCachePath = "${applicationContext().cacheDir}/"

    val imagesCacheSize: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            loadImagesCacheSize()
        }
    }

    private val appCacheSize: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            loadAppCacheSize()
        }
    }

    fun getImagesCacheSize(): LiveData<String> {
        return imagesCacheSize
    }

    fun getAppCacheSize(): LiveData<String> {
        return appCacheSize
    }

    private fun loadImagesCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            imagesCacheSize.postValue(imagesCachePath.getDirectorySize())
        }
    }

    private fun loadAppCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            appCacheSize.postValue(appCachePath.getDirectorySize())
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val file = File(appCachePath)
                if (file.deleteRecursively()) {
                    loadAppCacheSize()
                    loadImagesCacheSize()
                }
            }.onFailure {
                postError(it)
            }
        }
    }

    fun clearImagesData() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val file = File(imagesCachePath)
                if (file.deleteRecursively()) {
                    loadImagesCacheSize()
                    loadAppCacheSize()
                }
            }.onFailure {
                postError(it)
            }
        }
    }
}
