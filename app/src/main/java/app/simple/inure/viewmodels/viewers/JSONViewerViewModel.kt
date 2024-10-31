package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class JSONViewerViewModel(application: Application, private val accentColor: Int, private val packageInfo: PackageInfo, private val path: String, private val isRaw: Boolean = false)
    : WrappedViewModel(application) {

    private val quotations: Pattern = Pattern.compile(":\\s\"[\\S\\w^]*\"",
                                                      Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

    private val tags = Pattern.compile("\"[a-zA-Z_0-9]+\"+:",  // "a-z0-9":
                                       Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

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
                val formattedContent: SpannableString

                val code: String = getJsonFile()

                formattedContent = SpannableString(code)
                val matcher: Matcher = tags.matcher(code)
                while (matcher.find()) {
                    formattedContent.setSpan(ForegroundColorSpan(Color.parseColor("#2980B9")), matcher.start(),
                                             matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                matcher.usePattern(quotations)
                while (matcher.find()) {
                    formattedContent.setSpan(ForegroundColorSpan(accentColor),
                                             matcher.start(), matcher.end(),
                                             Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                spanned.postValue(formattedContent)
            }.getOrElse {
                postError(it)
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun getJsonFile(): String {
        if (isRaw) {
            // Open JSON file from file system
            File(path).inputStream().use { inputStream ->
                inputStream.bufferedReader().use {
                    return it.readText()
                }
            }
        } else {
            ZipFile(packageInfo.safeApplicationInfo.sourceDir).use { zipFile ->
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
        }

        throw FileNotFoundException("JSON file not found")
    }
}
