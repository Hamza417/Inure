package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.ExtrasPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ExtrasViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {

    init {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    var keyword: String = ""
        set(value) {
            field = value
            getExtrasData()
        }

    private val extras: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getExtrasData()
        }
    }

    fun getExtras(): LiveData<MutableList<String>> {
        return extras
    }

    private fun getExtrasData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(APKParser.getExtraFiles(packageInfo.applicationInfo.sourceDir, keyword)) {
                    if (this.isEmpty() && keyword.isEmpty()) throw NullPointerException()

                    extras.postValue(apply {
                        sortBy {
                            it.lowercase(Locale.getDefault())
                        }
                    })
                }
            }.getOrElse {
                if (it is NullPointerException) {
                    notFound.postValue(88)
                } else {
                    error.postValue(it)
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ExtrasPreferences.json,
            ExtrasPreferences.html,
            ExtrasPreferences.css,
            ExtrasPreferences.properties,
            ExtrasPreferences.js,
            ExtrasPreferences.tsv,
            ExtrasPreferences.txt,
            ExtrasPreferences.proto,
            ExtrasPreferences.java,
            ExtrasPreferences.bin,
            ExtrasPreferences.ttf,
            ExtrasPreferences.md,
            ExtrasPreferences.ini,
            -> {
                getExtrasData()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }
}