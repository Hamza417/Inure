package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.constants.Misc.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ExtrasViewModel(application: Application, val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val extras: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getExtrasData("")
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

    fun getExtrasData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
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
        }
    }
}