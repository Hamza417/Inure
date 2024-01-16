package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.text.Html
import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser.extractManifest
import app.simple.inure.apk.xml.XML
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.StringUtils.readTextSafely
import app.simple.inure.util.XMLUtils.formatXML
import app.simple.inure.util.XMLUtils.getPrettyXML
import com.jaredrummler.apkparser.ApkParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.io.FileInputStream

class XMLViewerViewModel(val packageInfo: PackageInfo,
                         private val isManifest: Boolean,
                         private val pathToXml: String,
                         private val raw: Boolean,
                         application: Application)

    : WrappedViewModel(application) {

    private val spanned: MutableLiveData<Spanned> by lazy {
        MutableLiveData<Spanned>().also {
            getSpannedXml()
        }
    }

    private val string: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            getStringXml()
        }
    }

    fun getSpanned(): LiveData<Spanned> {
        return spanned
    }

    fun getString(): LiveData<String> {
        return string
    }

    private fun getSpannedXml() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val code: String = if (raw) {
                    FileInputStream(File(pathToXml)).use {
                        it.readTextSafely()
                    }
                } else {
                    if (isManifest) {
                        packageInfo.applicationInfo.extractManifest()!!
                    } else {
                        kotlin.runCatching {
                            kotlin.runCatching {
                                ApkParser.create(packageInfo.applicationInfo.sourceDir.toFile()).use {
                                    it.transBinaryXml(pathToXml)
                                }
                            }.getOrElse {
                                ApkFile(packageInfo.applicationInfo.sourceDir.toFile()).use {
                                    it.transBinaryXml(pathToXml)
                                }
                            }
                        }.getOrElse {
                            XML(packageInfo.applicationInfo.sourceDir).use {
                                it.transBinaryXml(pathToXml)
                            }
                        }
                    }
                }

                spanned.postValue(code.formatXML().getPrettyXML())
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun getStringXml() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val code = if (isManifest) {
                    packageInfo.applicationInfo.extractManifest()!!
                } else {
                    XML(packageInfo.applicationInfo.sourceDir).use {
                        it.transBinaryXml(pathToXml)
                    }
                }

                val data = String.format(
                        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3" +
                                ".org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3" +
                                ".org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; " +
                                "charset=utf-8\" /><p style=\"word-wrap: break-word;\"><script src=\"prettyprint/run_prettify.js" +
                                "?skin=prettyprint/github\"></script></head><body bgcolor=\"transparent\"><pre class=\"prettyprint " +
                                "linenums\">%s</pre></body></html>", Html.escapeHtml(code))

                string.postValue(data)
            }.getOrElse {
                string.postValue(it.stackTraceToString())
            }
        }
    }
}
