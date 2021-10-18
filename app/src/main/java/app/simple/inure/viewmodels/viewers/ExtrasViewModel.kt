package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.constants.Misc.delay
import app.simple.inure.preferences.ExtrasPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.system.measureTimeMillis

class ExtrasViewModel(application: Application, val packageInfo: PackageInfo) : AndroidViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {

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

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getExtras(): LiveData<MutableList<String>> {
        return extras
    }

    fun getError(): LiveData<String> {
        return error
    }

    private fun getExtrasData() {
        viewModelScope.launch(Dispatchers.Default) {
            println(measureTimeMillis {
                kotlin.runCatching {
                    with(APKParser.getExtraFiles(packageInfo.applicationInfo.sourceDir, keyword)) {
                        extras.postValue(apply {
                            sortBy {
                                it.lowercase(Locale.getDefault())
                            }
                        })
                    }
                }.getOrElse {
                    delay(delay)
                    error.postValue(it.message)
                }
            })
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