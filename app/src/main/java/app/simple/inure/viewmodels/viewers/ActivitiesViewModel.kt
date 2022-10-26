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
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.ActivityInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivitiesViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val delay = 500L

    private val activities: MutableLiveData<MutableList<ActivityInfoModel>> by lazy {
        MutableLiveData<MutableList<ActivityInfoModel>>().also {
            getActivitiesData("")
        }
    }

    fun getActivities(): LiveData<MutableList<ActivityInfoModel>> {
        return activities
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
                    activityInfoModel.target = ai.targetActivity ?: getString(R.string.not_available)
                    activityInfoModel.exported = ai.exported
                    activityInfoModel.permission = ai.permission ?: getString(R.string.no_permissions_required)

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

                activities.postValue(list)
            }.getOrElse {
                if (it is NullPointerException) {
                    notFound.postValue(5)
                } else {
                    postError(it)
                }
            }
        }
    }
}