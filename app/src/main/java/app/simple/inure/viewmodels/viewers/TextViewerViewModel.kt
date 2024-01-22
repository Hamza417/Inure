package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.decoders.XMLDecoder
import app.simple.inure.util.StringUtils.readTextSafely
import app.simple.inure.util.XMLUtils.formatXML
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class TextViewerViewModel(private val packageInfo: PackageInfo, private val path: String, application: Application, val isRaw: Boolean)
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
                if (isRaw) {
                    // Open TXT file from file system
                    File(path).inputStream().use { inputStream ->
                        inputStream.bufferedReader().use {
                            text.postValue(it.readText())
                        }
                    }
                } else {
                    ZipFile(packageInfo.applicationInfo.sourceDir).use { zipFile ->
                        val entries: Enumeration<out ZipEntry?> = zipFile.entries()
                        while (entries.hasMoreElements()) {
                            entries.nextElement()!!.let { entry ->
                                kotlin.runCatching {
                                    if (entry.name == path) {
                                        when {
                                            path.endsWith("xml") -> {
                                                val xml = XMLDecoder(packageInfo.applicationInfo.sourceDir).decode(path)
                                                text.postValue(xml.formatXML())
                                            }
                                            else -> {
                                                text.postValue(BufferedInputStream(zipFile.getInputStream(entry)).readTextSafely())
                                            }
                                        }
                                    }
                                }.getOrElse {
                                    text.postValue(
                                            BufferedInputStream(zipFile.getInputStream(entry)).use { bufferedInputStream ->
                                                bufferedInputStream.bufferedReader().use {
                                                    it.readText()
                                                }
                                            })
                                }
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