package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.constants.Misc
import app.simple.inure.model.ServiceInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ServicesViewModel(application: Application, private val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val services: MutableLiveData<MutableList<ServiceInfoModel>> by lazy {
        MutableLiveData<MutableList<ServiceInfoModel>>().also {
            getServicesData("")
        }
    }

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getServices(): LiveData<MutableList<ServiceInfoModel>> {
        return services
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun getServicesData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<ServiceInfoModel>()

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.GET_SERVICES or PackageManager.MATCH_DISABLED_COMPONENTS
                } else {
                    @Suppress("deprecation")
                    PackageManager.GET_SERVICES or PackageManager.GET_DISABLED_COMPONENTS
                }

                for (info in getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName, flags).services) {
                    val serviceInfoModel = ServiceInfoModel()

                    serviceInfoModel.serviceInfo = info
                    serviceInfoModel.name = info.name
                    serviceInfoModel.isExported = info.exported
                    serviceInfoModel.flags = info.flags
                    serviceInfoModel.foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.foregroundServiceType else -3
                    serviceInfoModel.permissions = info.permission ?: getApplication<Application>().getString(R.string.no_permission_required)

                    with(StringBuilder()) {
                        append(" | ")
                        append(MetaUtils.getForegroundServiceType(serviceInfoModel.foregroundType, getApplication()))
                        append(" | ")
                        append(MetaUtils.getServiceFlags(info.flags, getApplication()))

                        serviceInfoModel.status = this.toString()
                    }

                    if (serviceInfoModel.name.lowercase().contains(keyword.lowercase())) {
                        list.add(serviceInfoModel)
                    }
                }

                services.postValue(list)
            }.getOrElse {
                delay(Misc.delay)
                error.postValue(it.message)
            }
        }
    }
}
