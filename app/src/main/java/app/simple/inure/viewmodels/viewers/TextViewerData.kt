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

    private val string: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            getString()
        }
    }

    fun getText(): LiveData<String> {
        return string
    }

    private fun getString() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000L)
            kotlin.runCatching {
                ZipFile(applicationInfo.sourceDir).use {
                    val entries: Enumeration<out ZipEntry?> = it.entries()
                    while (entries.hasMoreElements()) {
                        val entry: ZipEntry? = entries.nextElement()
                        val name: String = entry!!.name
                        if (name == path) {
                            string.postValue(
                                IOUtils.toString(BufferedInputStream(
                                    ZipFile(applicationInfo.sourceDir).getInputStream(entry)),
                                                 "UTF-8")
                            )
                        }
                    }
                }
            }.getOrElse {
                string.postValue(
                    it.stackTraceToString()
                )
            }
        }
    }
}
