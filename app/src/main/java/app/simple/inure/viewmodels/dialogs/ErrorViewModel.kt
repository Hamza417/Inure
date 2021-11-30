package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.text.toSpanned
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern

class ErrorViewModel(application: Application, private val error: String, val accentColor: Int) : AndroidViewModel(application) {

    private val tags = Pattern.compile("" /*Only for indentation */ +
                                               "\\(\\w+\\.+\\S+\\)",
                                       Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

    private val spanned: MutableLiveData<Spanned> by lazy {
        MutableLiveData<Spanned>().also {
            formatSpanned()
        }
    }

    fun getSpanned(): LiveData<Spanned> {
        return spanned
    }

    private fun formatSpanned() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val content = SpannableString(error)

                val matcher: Matcher = tags.matcher(error)
                while (matcher.find()) {
                    content.setSpan(ForegroundColorSpan(accentColor), matcher.start(),
                                    matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                spanned.postValue(content)
            }.onFailure {
                spanned.postValue(error.toSpanned())
            }
        }
    }
}