package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppSizeViewModel(application: Application) : AndroidViewModel(application) {

    private val totalAppSize: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>().also {
            loadTotalAppSize()
        }
    }

    private fun loadTotalAppSize() {
        viewModelScope.launch(Dispatchers.Default) {
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
}
