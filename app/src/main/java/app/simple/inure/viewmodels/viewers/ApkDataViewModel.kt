package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.FeatureInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApkDataViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val features: MutableLiveData<MutableList<FeatureInfo>> by lazy {
        MutableLiveData<MutableList<FeatureInfo>>().also {
            getFeaturesData()
        }
    }

    private val resources: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getResourceData("")
        }
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

                for (featureInfo in packageManager.getPackageInfo(packageInfo.packageName)!!.reqFeatures) {
                    list.add(featureInfo)
                }

                if (list.isEmpty()) throw NullPointerException()

                features.postValue(list)
            }.getOrElse {
                if (it is NullPointerException) {
                    notFound.postValue(55)
                } else {
                    postError(it)
                }
            }
        }
    }

    fun getResourceData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(APKParser.getXmlFiles(packageInfo.applicationInfo.sourceDir, keyword)) {
                    if (this.isEmpty() && keyword.isEmpty()) throw NullPointerException()
                    resources.postValue(this)
                }
            }.getOrElse {
                if (it is NullPointerException) {
                    notFound.postValue(3)
                } else {
                    postError(it)
                }
            }
        }
    }
}
