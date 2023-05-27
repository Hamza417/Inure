package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.ProviderInfoModel
import app.simple.inure.preferences.SearchPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProvidersViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val providers: MutableLiveData<MutableList<ProviderInfoModel>> by lazy {
        MutableLiveData<MutableList<ProviderInfoModel>>().also {
            if (SearchPreferences.isDeepSearchKeywordModeEnabled()) {
                getProvidersData(SearchPreferences.getLastSearchKeyword())
            } else {
                getProvidersData("")
            }
        }
    }

    fun getProviders(): LiveData<MutableList<ProviderInfoModel>> {
        return providers
    }

    fun getProvidersData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<ProviderInfoModel>()

                for (pi in packageManager.getPackageInfo(packageInfo.packageName)!!.providers) {
                    val providerInfoModel = ProviderInfoModel()
                    val signatures: Array<String> = context.resources.getStringArray(R.array.trackers)

                    providerInfoModel.providerInfo = pi
                    providerInfoModel.name = pi.name
                    providerInfoModel.authority = pi.authority
                    providerInfoModel.isExported = pi.exported
                    providerInfoModel.permissions = pi.readPermission + pi.writePermission

                    for (signature in signatures) {
                        if (pi.name!!.contains(signature)) {
                            providerInfoModel.trackingId = signature
                            break
                        }
                    }

                    with(StringBuilder()) {
                        append(" | ")
                        append(MetaUtils.getServiceFlags(pi.flags, application))

                        providerInfoModel.status = this.toString()
                    }

                    if (providerInfoModel.name.lowercase().contains(keyword.lowercase())) {
                        list.add(providerInfoModel)
                    }
                }

                list.sortBy {
                    it.name.substring(it.name.lastIndexOf(".") + 1)
                }

                providers.postValue(list)
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