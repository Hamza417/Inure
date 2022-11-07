package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.exceptions.LargeStringException
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.JavaSyntaxUtils.highlightJava
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.FileNotFoundException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class JavaViewModel(application: Application, val accentColor: Int, val packageInfo: PackageInfo, val path: String) : WrappedViewModel(application) {

    private val spanned: MutableLiveData<Spanned> by lazy {
        MutableLiveData<Spanned>().also {
            getSpannedXml()
        }
    }

    fun getSpanned(): LiveData<Spanned> {
        return spanned
    }

    private fun getSpannedXml() {
        viewModelScope.launch(Dispatchers.IO) {

            delay(500L)

            kotlin.runCatching {
                val code: String = getJavaFile()

                if (code.length >= 150000 && !FormattingPreferences.isLoadingLargeStrings()) {
                    throw LargeStringException("String size ${code.length} is too big to render without freezing the app")
                }

                val formattedContent = code.highlightJava()

                spanned.postValue(formattedContent)
            }.getOrElse {
                postError(it)
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun getJavaFile(): String {
        ZipFile(packageInfo.applicationInfo.sourceDir).use { zipFile ->
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()

            while (entries.hasMoreElements()) {
                entries.nextElement()!!.let { entry ->
                    if (entry.name == path) {
                        return BufferedInputStream(zipFile.getInputStream(entry)).use { bufferedInputStream ->
                            bufferedInputStream.bufferedReader().use {
                                it.readText()
                            }
                        }
                    }
                }
            }
        }

        throw FileNotFoundException("file at $path not found")
    }
}