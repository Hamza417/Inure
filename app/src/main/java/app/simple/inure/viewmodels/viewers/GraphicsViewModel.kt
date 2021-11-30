package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.constants.Misc
import app.simple.inure.preferences.GraphicsPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class GraphicsViewModel(application: Application, val packageInfo: PackageInfo) : AndroidViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {

    init {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    var keyword: String = ""
        set(value) {
            field = value
            getGraphicsData()
        }

    private val graphics: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getGraphicsData()
        }
    }

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun getGraphics(): LiveData<MutableList<String>> {
        return graphics
    }

    fun getGraphicsData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(APKParser.getGraphicsFiles(packageInfo.applicationInfo.sourceDir, keyword)) {
                    graphics.postValue(apply {
                        sortBy {
                            it.lowercase(Locale.getDefault())
                        }
                    })
                }
            }.getOrElse {
                delay(Misc.delay)
                error.postValue(it.stackTraceToString())
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GraphicsPreferences.png,
            GraphicsPreferences.jpg,
            GraphicsPreferences.jpeg,
            GraphicsPreferences.gif,
            GraphicsPreferences.webp,
            GraphicsPreferences.svg,
            -> {
                getGraphicsData()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }
}