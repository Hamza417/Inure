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
import app.simple.inure.models.ActivityInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReceiversViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val receivers: MutableLiveData<MutableList<ActivityInfoModel>> by lazy {
        MutableLiveData<MutableList<ActivityInfoModel>>().also {
            getReceiversData("")
        }
    }

    fun getReceivers(): LiveData<MutableList<ActivityInfoModel>> {
        return receivers
    }

    fun getReceiversData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<ActivityInfoModel>()

                for (ai in getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName)!!.receivers) {
                    val activityInfoModel = ActivityInfoModel()

                    activityInfoModel.activityInfo = ai
                    activityInfoModel.name = ai.name
                    activityInfoModel.target = ai.targetActivity ?: getApplication<Application>().getString(R.string.not_available)
                    activityInfoModel.exported = ai.exported
                    activityInfoModel.permission = ai.permission ?: getApplication<Application>().getString(R.string.no_permissions_required)

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
                if (it is NullPointerException) {
                    notFound.postValue(9)
                } else {
                    postError(it)
                }
            }
        }
    }
}