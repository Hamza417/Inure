package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.FeatureInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.SearchPreferences
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
            if (SearchPreferences.isSearchKeywordModeEnabled()) {
                getResourceData(SearchPreferences.getLastSearchKeyword())
            } else {
                getResourceData("")
            }
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
                val isInstalled = packageManager.isPackageInstalled(packageInfo.packageName)

                for (featureInfo in getPackageInfo(isInstalled).reqFeatures) {
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

    private fun getPackageInfo(isInstalled: Boolean): PackageInfo {
        return if (isInstalled) {
            packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_META_DATA)!!
        } else {
            packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir, PackageManager.GET_META_DATA)!!
        }
    }
}
