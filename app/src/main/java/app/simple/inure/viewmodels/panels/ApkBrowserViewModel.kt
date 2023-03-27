package app.simple.inure.viewmodels.panels

import android.app.Application
import android.os.Environment
import android.util.Log
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

    fun getApkPaths(): MutableLiveData<ArrayList<String>> {
        return paths
    }

    private fun loadApkPaths() {
        viewModelScope.launch(Dispatchers.IO) {
            val externalStorage = Environment.getExternalStorageDirectory()
            val apkPaths = ArrayList<String>()

            externalStorage.walkTopDown().forEach {
                Log.d("APK", it.absolutePath)
                if (it.extension == "apk" || it.extension == "apks" || it.extension == "apkm") {
                    apkPaths.add(it.absolutePath)
                }
            }

            paths.postValue(apkPaths)
        }
    }
}