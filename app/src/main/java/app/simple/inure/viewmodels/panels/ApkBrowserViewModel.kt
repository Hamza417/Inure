package app.simple.inure.viewmodels.panels

import android.app.Application
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApkBrowserViewModel(application: Application) : WrappedViewModel(application) {

    private val paths: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>().also {
            loadApkPaths()
        }
    }

    private val info: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getApkPaths(): LiveData<ArrayList<String>> {
        return paths
    }

    fun getPathInfo(): LiveData<String> {
        return info
    }

    private fun loadApkPaths() {
        viewModelScope.launch(Dispatchers.IO) {
            val externalStorage = Environment.getExternalStorageDirectory()
            val apkPaths = ArrayList<String>()

            externalStorage.walkTopDown().forEach {
                info.postValue(it.absolutePath.substringBeforeLast("/"))

                if (it.extension == "apk" || it.extension == "apks" || it.extension == "apkm") {
                    apkPaths.add(it.absolutePath)
                }
            }

            paths.postValue(apkPaths)
        }
    }

    fun refresh() {
        loadApkPaths()
    }
}