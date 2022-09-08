package app.simple.inure.viewmodels.subviewers

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import android.text.Spannable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.models.ServiceInfoModel
import app.simple.inure.util.StringUtils.applyAccentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServiceInfoViewModel(application: Application, private val serviceInfoModel: ServiceInfoModel, private val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val serviceInfo: MutableLiveData<ArrayList<Pair<Int, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<Int, Spannable>>>().also {
            loadData()
        }
    }

    fun getServicesInfo(): LiveData<ArrayList<Pair<Int, Spannable>>> {
        return serviceInfo
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            serviceInfo.postValue(arrayListOf(
                    getForegroundService(),
                    getFlags()
            ))
        }
    }

    private fun getForegroundService(): Pair<Int, Spannable> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Pair(R.string.foreground_service_type,
                 MetaUtils.getForegroundServiceType(serviceInfoModel.serviceInfo.foregroundServiceType, getApplication()).applyAccentColor())
        } else {
            Pair(R.string.foreground_service_type,
                 MetaUtils.getForegroundServiceType(0, getApplication()).applyAccentColor())
        }
    }

    private fun getFlags(): Pair<Int, Spannable> {
        return Pair(R.string.flags,
                    MetaUtils.getFlags(serviceInfoModel.serviceInfo.flags, getApplication()).applyAccentColor())
    }
}