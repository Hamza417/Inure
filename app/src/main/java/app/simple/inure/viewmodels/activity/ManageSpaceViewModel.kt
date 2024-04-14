package app.simple.inure.viewmodels.activity

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.FileSizeHelper.getDirectorySize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ManageSpaceViewModel(application: Application) : WrappedViewModel(application) {

    private val trackersCachePath = "${applicationContext().cacheDir}/trackers_cache/"
    private val imagesCachePath = "${applicationContext().cacheDir}/image_manager_disk_cache/"
    private val appCachePath = "${applicationContext().cacheDir}/"

    private val trackersCacheSize: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            loadTrackersCacheSize()
        }
    }

    val imagesCacheSize: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            loadImagesCacheSize()
        }
    }

    private val appCacheSize: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            viewModelScope.launch(Dispatchers.IO) {
                appCacheSize.postValue(appCachePath.getDirectorySize())
            }
        }
    }

    fun getTrackersCacheSize(): LiveData<String> {
        return trackersCacheSize
    }

    fun getImagesCacheSize(): LiveData<String> {
        return imagesCacheSize
    }

    fun getAppCacheSize(): LiveData<String> {
        return appCacheSize
    }

    fun clearTrackersData() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val file = File(trackersCachePath)
                if (file.deleteRecursively()) {
                    loadTrackersCacheSize()
                }
            }.onFailure {
                it.printStackTrace()
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
                }
            }.onFailure {
                postError(it)
            }
        }
    }

    private fun loadTrackersCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                trackersCacheSize.postValue(trackersCachePath.getDirectorySize())
            }
        }
    }

    private fun loadImagesCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imagesCacheSize.postValue(imagesCachePath.getDirectorySize())
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val file = File(appCachePath)
                if (file.deleteRecursively()) {
                    appCacheSize.postValue(appCachePath.getDirectorySize())
                    loadImagesCacheSize()
                }
            }.onFailure {
                postError(it)
            }
        }
    }
}
