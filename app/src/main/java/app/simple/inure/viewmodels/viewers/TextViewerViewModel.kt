package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.xml.XML
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class TextViewerViewModel(private val packageInfo: PackageInfo, private val path: String, application: Application)
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
            kotlin.runCatching {
                ZipFile(packageInfo.applicationInfo.sourceDir).use { zipFile ->
                    val entries: Enumeration<out ZipEntry?> = zipFile.entries()
                    while (entries.hasMoreElements()) {
                        entries.nextElement()!!.let { entry ->
                            kotlin.runCatching {
                                if (entry.name == path) {
                                    when {
                                        path.endsWith("xml") -> {
                                            text.postValue(
                                                    XML(packageInfo.applicationInfo.sourceDir).use {
                                                        it.transBinaryXml(path)
                                                    })
                                        }
                                        else -> {
                                            text.postValue(
                                                    IOUtils.toString(
                                                            BufferedInputStream(zipFile.getInputStream(entry)),
                                                            "UTF-8"))
                                        }
                                    }
                                }
                            }.getOrElse {
                                text.postValue(
                                        IOUtils.toString(
                                                BufferedInputStream(zipFile.getInputStream(entry)),
                                                "UTF-8"))
                            }
                        }
                    }
                }
            }.getOrElse {
                text.postValue(it.stackTraceToString())
            }
        }
    }
}