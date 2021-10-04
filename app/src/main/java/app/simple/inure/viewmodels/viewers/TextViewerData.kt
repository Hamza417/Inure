package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class TextViewerData(private val applicationInfo: ApplicationInfo, private val path: String, application: Application)
    : AndroidViewModel(application) {

    private val text: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            getString()
        }
    }

    fun getText(): LiveData<String> {
        return text
    }

    private fun getString() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000L)
            kotlin.runCatching {
                ZipFile(applicationInfo.sourceDir).use { zipFile ->
                    val entries: Enumeration<out ZipEntry?> = zipFile.entries()

                    while (entries.hasMoreElements()) {
                        entries.nextElement()!!.let { entry ->
                            if (entry.name == path) {
                                text.postValue(
                                    IOUtils.toString(
                                        BufferedInputStream(zipFile.getInputStream(entry)),
                                        "UTF-8"))
                            }
                        }
                    }
                }
            }.getOrElse {
                text.postValue(
                    it.stackTraceToString()
                )
            }
        }
    }
}
