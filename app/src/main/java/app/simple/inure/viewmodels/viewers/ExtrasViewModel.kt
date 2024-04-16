package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.Extra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class ExtrasViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    var keyword: String = ""
        set(value) {
            field = value
            getExtrasData()
        }

    private val extras: MutableLiveData<MutableList<Extra>> by lazy {
        MutableLiveData<MutableList<Extra>>().also {
            getExtrasData()
        }
    }

    fun getExtras(): LiveData<MutableList<Extra>> {
        return extras
    }

    private fun getExtrasData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(APKParser.getExtraFiles(packageInfo.applicationInfo.sourceDir, keyword)) {
                    if (this.isEmpty() && keyword.isEmpty()) throw NullPointerException()

                    extras.postValue(apply {
                        sortBy {
                            it.name.lowercase(Locale.getDefault())
                        }
                    })
                }
            }.getOrElse {
                if (it is NullPointerException) {
                    notFound.postValue(88)
                } else {
                    postError(it)
                }
            }
        }
    }
}
