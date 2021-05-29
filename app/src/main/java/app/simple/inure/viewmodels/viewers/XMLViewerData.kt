package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.text.toSpanned
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.util.APKParser.extractManifest
import app.simple.inure.util.APKParser.getTransBinaryXml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern

class XMLViewerData(val applicationInfo: ApplicationInfo, private val isManifest: Boolean, private val pathToXml: String, application: Application, val accentColor: Int)
    : AndroidViewModel(application) {

    private val quotations: Pattern = Pattern.compile("\"([^\"]*)\"", Pattern.MULTILINE)

    private val tags = Pattern.compile("" /*Only for indentation */ +
                                               "\\<\\w+\\.+\\S+" + // <xml.yml.zml>
                                               "|\\<\\w+\\.+\\S+" + // <xml.yml.zml...nthml
                                               "|\\<\\/\\w+.+\\>" + // </xml.yml.zml>
                                               "|\\<\\/\\w+\\-+\\S+\\>" + // </xml-yml>
                                               "|\\<\\w+\\-+\\S+" + // <xml-yml-zml...nthml
                                               "|\\</\\w+>" + // </xml>
                                               "|\\</\\w+" + // </xml
                                               "|\\<\\w+\\/>" + // <xml/>
                                               "|\\<\\w+\\>" +  // <xml>
                                               "|\\<\\w+" +  // <xml
                                               "|\\<.\\w+" + // <?xml
                                               "|\\?\\>" + // ?>
                                               "|\\/>", // />
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

            delay(1000L)

            kotlin.runCatching {
                val formattedContent: SpannableString

                val code: String = if (isManifest) {
                    applicationInfo.extractManifest()!!
                } else {
                    applicationInfo.getTransBinaryXml(pathToXml)
                }


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
                spanned.postValue(it.stackTraceToString().toSpanned())
            }
        }
    }

    private fun getStringXml() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000) // Lets the animations finish first

            kotlin.runCatching {
                val code = if (isManifest) {
                    applicationInfo.extractManifest()!!
                } else {
                    applicationInfo.getTransBinaryXml(pathToXml)
                }

                val data = String.format(
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3" +
                            ".org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3" +
                            ".org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; " +
                            "charset=utf-8\" /><p style=\"word-wrap: break-word;\"><script src=\"run_prettify.js" +
                            "?skin=github\"></script></head><body bgcolor=\"transparent\"><pre class=\"prettyprint " +
                            "linenums\">%s</pre></body></html>", Html.escapeHtml(code))

                string.postValue(data)
            }.getOrElse {
                string.postValue(it.stackTraceToString())
            }
        }
    }
}