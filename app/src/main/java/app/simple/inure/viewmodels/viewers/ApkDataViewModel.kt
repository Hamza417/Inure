package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.FeatureInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.constants.Misc.delay
import com.jaredrummler.apkparser.model.AndroidComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.util.*

class ApkDataViewModel(application: Application, val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val features: MutableLiveData<MutableList<FeatureInfo>> by lazy {
        MutableLiveData<MutableList<FeatureInfo>>().also {
            getFeaturesData()
        }
    }

    private val resources: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getResourceData()
        }
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun getFeatures(): LiveData<MutableList<FeatureInfo>> {
        return features
    }

    fun getResources(): LiveData<MutableList<String>> {
        return resources
    }

    private fun getFeaturesData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<FeatureInfo>()

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.GET_CONFIGURATIONS or PackageManager.MATCH_DISABLED_COMPONENTS
                } else {
                    @Suppress("deprecation")
                    PackageManager.GET_CONFIGURATIONS or PackageManager.GET_DISABLED_COMPONENTS
                }

                for (featureInfo in getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName, flags).reqFeatures) {
                    list.add(featureInfo)
                }

                features.postValue(list)
            }.getOrElse {
                delay(delay)
                error.postValue(it.stackTraceToString())
            }
        }
    }

    private fun getResourceData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(APKParser.getXmlFiles(packageInfo.applicationInfo.sourceDir)) {
                    if (size == 0) {
                        throw FileNotFoundException("This package does not contain any xml resource files")
                    } else {
                        resources.postValue(apply {
                            sortBy {
                                it.lowercase(Locale.getDefault())
                            }
                        })
                    }
                }
            }.getOrElse {
                delay(delay)
                error.postValue(it.stackTraceToString())
            }
        }
    }

    /**
     * For some reason this did not work.
     *
     * TODO - add explanation to why
     */
    @Deprecated("This won't work",
                ReplaceWith("this.apply { sortedBy { it.name.substring(it.name.lastIndexOf(\".\")) } }"))
    private fun MutableList<AndroidComponent>.sort(): MutableList<AndroidComponent> {
        return this.apply {
            sortedBy {
                it.name.substring(it.name.lastIndexOf("."))
            }
        }
    }
}
