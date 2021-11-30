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
import app.simple.inure.model.ActivityInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReceiversViewModel(application: Application, val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val receivers: MutableLiveData<MutableList<ActivityInfoModel>> by lazy {
        MutableLiveData<MutableList<ActivityInfoModel>>().also {
            getReceiversData("")
        }
    }

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getReceivers(): LiveData<MutableList<ActivityInfoModel>> {
        return receivers
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun getReceiversData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<ActivityInfoModel>()

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.GET_RECEIVERS or PackageManager.MATCH_DISABLED_COMPONENTS
                } else {
                    @Suppress("deprecation")
                    PackageManager.GET_RECEIVERS or PackageManager.GET_DISABLED_COMPONENTS
                }

                for (ai in getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName, flags).receivers) {
                    val activityInfoModel = ActivityInfoModel()

                    activityInfoModel.activityInfo = ai
                    activityInfoModel.name = ai.name
                    activityInfoModel.target = ai.targetActivity ?: getApplication<Application>().getString(R.string.not_available)
                    activityInfoModel.exported = ai.exported
                    activityInfoModel.permission = ai.permission ?: getApplication<Application>().getString(R.string.no_permission_required)

                    with(StringBuilder()) {
                        append(" | ")
                        append(MetaUtils.getLaunchMode(ai.launchMode, getApplication()))
                        append(" | ")
                        append(MetaUtils.getOrientation(ai.screenOrientation, getApplication()))
                        activityInfoModel.status = this.toString()
                    }

                    if (activityInfoModel.name.lowercase().contains(keyword.lowercase())) {
                        list.add(activityInfoModel)
                    }
                }

                list.sortBy {
                    it.name.substring(it.name.lastIndexOf(".") + 1)
                }

                receivers.postValue(list)
            }.getOrElse {
                delay(Misc.delay)
                error.postValue(it.stackTraceToString())
            }
        }
    }
}