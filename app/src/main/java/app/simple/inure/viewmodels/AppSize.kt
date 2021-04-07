package app.simple.inure.viewmodels

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AppSize(application: Application)
    : AndroidViewModel(application), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    private val totalAppSize: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>().also {
            loadTotalAppSize()
        }
    }

    private fun loadTotalAppSize() {
        launch {
            val apps = getApplication<Application>()
                    .applicationContext.packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA) as ArrayList

            var size = 0L

            for (i in apps.indices) {
                size += apps[i].sourceDir.getDirectoryLength()
            }

            totalAppSize.postValue(size)
        }
    }

    fun getTotalAppSize(): LiveData<Long> {
        return totalAppSize
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}