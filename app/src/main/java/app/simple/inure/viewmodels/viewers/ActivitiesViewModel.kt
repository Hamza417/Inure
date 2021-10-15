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
import app.simple.inure.model.ActivityInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivitiesViewModel(application: Application, val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val delay = 500L

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val activities: MutableLiveData<MutableList<ActivityInfoModel>> by lazy {
        MutableLiveData<MutableList<ActivityInfoModel>>().also {
            getActivitiesData("")
        }
    }

    fun getActivities(): LiveData<MutableList<ActivityInfoModel>> {
        return activities
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun getActivitiesData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<ActivityInfoModel>()

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS
                } else {
                    @Suppress("deprecation")
                    PackageManager.GET_ACTIVITIES or PackageManager.GET_DISABLED_COMPONENTS
                }

                for (ai in getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName, flags).activities) {
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

                    if (keyword.isBlank()) {
                        list.add(activityInfoModel)
                    } else {
                        if (activityInfoModel.name.lowercase().contains(keyword)) {
                            list.add(activityInfoModel)
                        }
                    }
                }

                activities.postValue(list)
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }
}