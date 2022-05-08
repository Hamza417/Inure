package app.simple.inure.viewmodels.subviewers

import android.app.Application
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.trackers.dex.DexLoaderBuilder
import app.simple.inure.trackers.reflector.Reflector
import app.simple.inure.trackers.utils.UriUtils
import app.simple.inure.util.IOUtils
import dalvik.system.DexClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

class TrackerSourceViewModel(application: Application, val className: String, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val sourceData: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            loadSource()
        }
    }

    fun getSourceData(): LiveData<String> {
        return sourceData
    }

    private fun loadSource() {
        viewModelScope.launch(Dispatchers.IO) {
            parseSource()
        }
    }

    private fun parseSource() {
        var uriStream: InputStream? = null

        try {
            uriStream = UriUtils.getStreamFromUri(context, Uri.fromFile(File(packageInfo.applicationInfo.publicSourceDir)))

            val bytes = IOUtils.toByteArray(uriStream)
            val loader: DexClassLoader = DexLoaderBuilder.fromBytes(context, bytes)
            val loadClass: Class<*> = loader.loadClass(className)
            val reflector = Reflector(loadClass)

            reflector.generateClassData()

            sourceData.postValue(reflector.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            uriStream?.close()
        }
    }
}