package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.constants.Misc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class GraphicsViewModel(application: Application, val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val graphics: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getGraphicsData("")
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

    fun getGraphicsData(keyword: String) {
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
                error.postValue(it.message)
            }
        }
    }
}