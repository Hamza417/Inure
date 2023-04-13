package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.util.ActivityUtils
import app.simple.inure.util.ConditionUtils.isZero
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackersViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    var keyword: String = ""

    private val trackers: MutableLiveData<ArrayList<ActivityInfoModel>> by lazy {
        MutableLiveData<ArrayList<ActivityInfoModel>>().also {
            scanTrackers()
        }
    }

    private val activityInfo: MutableLiveData<Pair<ActivityInfoModel, Int>> by lazy {
        MutableLiveData<Pair<ActivityInfoModel, Int>>()
    }

    fun getTrackers(): LiveData<ArrayList<ActivityInfoModel>> {
        return trackers
    }

    fun getActivityInfo(): LiveData<Pair<ActivityInfoModel, Int>> {
        return activityInfo
    }

    private fun scanTrackers() {
        viewModelScope.launch(Dispatchers.IO) {
            val trackerSignatures = getTrackerSignatures()
            val activities = packageManager.getPackageInfo(packageInfo.packageName)!!.activities
            val trackersList = arrayListOf<ActivityInfoModel>()

            Log.d("TrackersViewModel", "scanTrackers: ${trackerSignatures.size}")

            if (activities != null) {
                for (activity in activities) {
                    Log.d("TrackersViewModel", "scanTrackers: ${activity.name}")
                    for (signature in trackerSignatures) {
                        if (activity.name.contains(signature)) {
                            Log.d("TrackersViewModel", "scanTrackers: ${activity.name}")
                            val activityInfoModel = ActivityInfoModel()

                            activityInfoModel.activityInfo = activity
                            activityInfoModel.name = activity.name
                            activityInfoModel.target = activity.targetActivity ?: getString(R.string.not_available)
                            activityInfoModel.isEnabled = ActivityUtils.isEnabled(applicationContext(), packageInfo.packageName, activity.name)
                            activityInfoModel.trackerId = signature

                            trackersList.add(activityInfoModel)

                            break
                        }
                    }
                }
            }

            trackersList.sortBy {
                it.name.substring(it.name.lastIndexOf(".") + 1)
            }

            if (trackersList.size.isZero()) {
                postWarning(getString(R.string.no_trackers_found))
            }

            trackers.postValue(trackersList)
        }
    }

    private fun getTrackerSignatures(): Array<out String> {
        return applicationContext().resources.getStringArray(R.array.trackers)
    }

    fun updateTrackersStatus(activityInfoModel: ActivityInfoModel, enabled: Boolean, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Shell.cmd("pm ${if (enabled) "enable" else "disable"} ${packageInfo.packageName}/${activityInfoModel.name}").exec().let {
                if (it.isSuccess) {
                    activityInfoModel.isEnabled = enabled
                    activityInfo.postValue(Pair(activityInfoModel, position))
                }
            }
        }
    }

    fun clearActivityInfo() {
        activityInfo.postValue(null)
    }
}