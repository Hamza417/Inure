package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.graphics.Typeface
import android.text.Spanned
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.Quotes
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ColorUtils.toHexColor
import app.simple.inure.util.TTFHelper
import app.simple.inure.util.TextViewUtils.toHtmlSpanned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FontViewModel(application: Application, val path: String, val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val quote: MutableLiveData<Spanned> by lazy {
        MutableLiveData<Spanned>()
    }

    private val typeface: MutableLiveData<Typeface> by lazy {
        MutableLiveData<Typeface>().also {
            setFont()
        }
    }

    fun getQuote(): LiveData<Spanned> {
        return quote
    }

    fun getTypeFace(): LiveData<Typeface> {
        return typeface
    }

    private fun setQuote() {
        val spanned = Quotes.quotes.random().replace(
                "%%%", AppearancePreferences.getAccentColor().toHexColor())
                .toHtmlSpanned()

        quote.postValue(spanned)
    }

    private fun setFont() {
        viewModelScope.launch(Dispatchers.IO) {
            val typeFace = TTFHelper
                    .getTTFFile(
                        path,
                        packageInfo, getApplication())

            this@FontViewModel.typeface.postValue(typeFace)

            setQuote()
        }
    }
}