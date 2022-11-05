package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.BuildConfig
import app.simple.inure.exceptions.LargeStringException
import app.simple.inure.extensions.viewmodels.RootServiceViewModel
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.FormattingPreferences
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.regex.Matcher
import java.util.regex.Pattern

class SharedPreferencesViewerViewModel(private val pathToXml: String, application: Application) : RootServiceViewModel(application) {

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
        MutableLiveData<Spanned>()
    }

    fun getSpanned(): LiveData<Spanned> {
        return spanned
    }

    private fun loadSharedPrefsFile(fileSystemManager: FileSystemManager?) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                kotlin.runCatching {
                    Shell.enableVerboseLogging = BuildConfig.DEBUG
                    Shell.setDefaultBuilder(Shell.Builder.create()
                                                .setContext(applicationContext())
                                                .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                                .setTimeout(10))
                }.onFailure {
                    Log.e(javaClass.name, "Failed to initialize Shell", it)
                }

                val code = fileSystemManager?.getSharedPrefsString()!!
                val formattedContent = SpannableString(code)

                if (code.length >= 150000 && !FormattingPreferences.isLoadingLargeStrings()) {
                    throw LargeStringException("String size ${code.length} is too big to render without freezing the app")
                }

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

    private fun FileSystemManager.getSharedPrefsString(): String {
        val channel = openChannel(pathToXml, FileSystemManager.MODE_READ_WRITE)
        val capacity = channel.size()
        val byteBuffer = ByteBuffer.allocate(capacity.toInt())
        channel.read(byteBuffer)
        return String(byteBuffer.array(), Charset.defaultCharset())
    }

    override fun runRootProcess(fileSystemManager: FileSystemManager?) {
        loadSharedPrefsFile(fileSystemManager)
    }
}