package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.graphics.Color
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser.extractManifest
import app.simple.inure.apk.parsers.ApkManifestFetcher
import app.simple.inure.apk.xml.XML
import app.simple.inure.exceptions.LargeStringException
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.StringUtils.readTextSafely
import app.simple.inure.util.XMLUtils.formatXML
import com.jaredrummler.apkparser.ApkParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.io.FileInputStream
import java.util.regex.Matcher
import java.util.regex.Pattern

class XMLViewerViewModel(val packageInfo: PackageInfo,
                         private val isManifest: Boolean,
                         private val pathToXml: String,
                         private val raw: Boolean,
                         application: Application)

    : WrappedViewModel(application) {

    private val quotations: Pattern = Pattern.compile("\"([^\"]*)\"", Pattern.MULTILINE)

    @Suppress("RegExpDuplicateAlternationBranch")
    private val tags = Pattern.compile("" /*Only for indentation */ +
                                               "<\\w+\\.+\\S+" + // <xml.yml.zml>
                                               "|<\\w+\\.+\\S+" + // <xml.yml.zml...nthml
                                               "|</\\w+.+>" + // </xml.yml.zml>
                                               "|</\\w+-+\\S+>" + // </xml-yml>
                                               "|<\\w+-+\\S+" + // <xml-yml-zml...nthml
                                               "|</\\w+>" + // </xml>
                                               "|</\\w+" + // </xml
                                               "|<\\w+/>" + // <xml/>
                                               "|<\\w+>" +  // <xml>
                                               "|<\\w+" +  // <xml
                                               "|<.\\w+" + // <?xml
                                               "|\\?>" + // ?>
                                               "|/>", // />
                                       Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

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
                val formattedContent: SpannableString

                var code: String = if (raw) {
                    FileInputStream(File(pathToXml)).use {
                        it.readTextSafely()
                    }
                } else {
                    if (isManifest) {
                        kotlin.runCatching {
                            packageInfo.applicationInfo.extractManifest()!!
                        }.getOrElse {
                            /**
                             * Alternate engine for parsing manifest
                             */
                            ApkManifestFetcher.getManifestXmlFromFilePath(packageInfo.applicationInfo.sourceDir)!!
                        }
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

                if (code.length >= 150000 && !FormattingPreferences.isLoadingLargeStrings()) {
                    throw LargeStringException("String size ${code.length} is too big to render without freezing the app")
                }

                code = code.formatXML()

                formattedContent = SpannableString(code)
                val matcher: Matcher = tags.matcher(code)
                while (matcher.find()) {
                    formattedContent.setSpan(ForegroundColorSpan(Color.parseColor("#2980B9")), matcher.start(),
                                             matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                matcher.usePattern(quotations)
                while (matcher.find()) {
                    formattedContent.setSpan(ForegroundColorSpan(AppearancePreferences.getAccentColor()),
                                             matcher.start(), matcher.end(),
                                             Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                spanned.postValue(formattedContent)
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun getStringXml() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(500) // Lets the animations finish first

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
