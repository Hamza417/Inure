package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.Graphic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class GraphicsViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    var keyword: String = ""
        set(value) {
            field = value
            getGraphicsData()
        }

    private val graphics: MutableLiveData<MutableList<Graphic>> by lazy {
        MutableLiveData<MutableList<Graphic>>().also {
            getGraphicsData()
        }
    }

    fun getGraphics(): LiveData<MutableList<Graphic>> {
        return graphics
    }

    private fun getGraphicsData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(APKParser.getGraphicsFiles(packageInfo.safeApplicationInfo.sourceDir, keyword)) {
                    if (this.isEmpty() && keyword.isEmpty()) throw NullPointerException()

                    graphics.postValue(apply {
                        sortBy {
                            it.name.lowercase(Locale.getDefault())
                        }
                    })
                }
            }.getOrElse {
                if (it is NullPointerException) {
                    notFound.postValue(666)
                } else {
                    postError(it)
                }
            }
        }
    }
}
