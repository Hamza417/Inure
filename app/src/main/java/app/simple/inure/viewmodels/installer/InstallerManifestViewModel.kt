package app.simple.inure.viewmodels.installer

import android.app.Application
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.ApkManifestFetcher
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.XMLUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern

class InstallerManifestViewModel(application: Application, private val file: File) : WrappedViewModel(application) {

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

    fun getSpanned(): LiveData<Spanned> {
        return spanned
    }

    private fun getSpannedXml() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val formattedContent: SpannableString

                val code: String = kotlin.runCatching {
                    ApkFile(file).use {
                        it.manifestXml
                    }
                }.getOrElse {
                    /**
                     * Alternate engine for parsing manifest
                     */
                    XMLUtils.getProperXml(ApkManifestFetcher.getManifestXmlFromFile(file)!!)!!
                }

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
                it.printStackTrace()
            }
        }
    }
}