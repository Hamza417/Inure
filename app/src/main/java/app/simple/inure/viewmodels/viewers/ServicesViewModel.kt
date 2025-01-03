package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.ServiceInfoModel
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.TrackerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServicesViewModel(application: Application, private val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val services: MutableLiveData<MutableList<ServiceInfoModel>> by lazy {
        MutableLiveData<MutableList<ServiceInfoModel>>().also {
            if (SearchPreferences.isSearchKeywordModeEnabled()) {
                getServicesData(SearchPreferences.getLastSearchKeyword())
            } else {
                getServicesData("")
            }
        }
    }

    fun getServices(): LiveData<MutableList<ServiceInfoModel>> {
        return services
    }

    fun getServicesData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<ServiceInfoModel>()
                val signatures = TrackerUtils.getTrackerSignatures()
                val isInstalled = packageManager.isPackageInstalled(packageInfo.packageName)

                for (info in getPackageInfo(isInstalled).services!!) {
                    val serviceInfoModel = ServiceInfoModel()

                    serviceInfoModel.serviceInfo = info
                    serviceInfoModel.name = info.name
                    serviceInfoModel.isExported = info.exported
                    serviceInfoModel.flags = info.flags
                    serviceInfoModel.foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.foregroundServiceType else -3
                    serviceInfoModel.permissions = info.permission ?: getString(R.string.no_permissions_required)

                    for (signature in signatures) {
                        if (serviceInfoModel.serviceInfo.name!!.contains(signature)) {
                            serviceInfoModel.trackerId = signature
                            break
                        }
                    }

                    with(StringBuilder()) {
                        append(" | ")
                        append(MetaUtils.getForegroundServiceType(serviceInfoModel.foregroundType, applicationContext()))
                        append(" | ")
                        append(MetaUtils.getServiceFlags(info.flags, applicationContext()))

                        serviceInfoModel.status = this.toString()
                    }

                    if (serviceInfoModel.name.lowercase().contains(keyword.lowercase())) {
                        list.add(serviceInfoModel)
                    }
                }

                list.sortBy {
                    it.name.substring(it.name.lastIndexOf(".") + 1)
                }

                services.postValue(list)
            }.getOrElse {
                if (it is NullPointerException) {
                    notFound.postValue(4)
                } else {
                    postError(it)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getPackageInfo(isInstalled: Boolean): PackageInfo {
        return if (isInstalled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                packageManager.getPackageInfo(packageInfo.packageName,
                                              PackageManager.GET_SERVICES or PackageManager.MATCH_DISABLED_COMPONENTS)!!
            } else {
                packageManager.getPackageInfo(packageInfo.packageName,
                                              PackageManager.GET_SERVICES or PackageManager.GET_DISABLED_COMPONENTS)!!
            }
        } else {
            packageManager.getPackageArchiveInfo(packageInfo.safeApplicationInfo.sourceDir,
                                                 PackageManager.GET_SERVICES or PackageManager.GET_DISABLED_COMPONENTS)!!
        }
    }
}
