package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.models.ServiceInfoModel
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.shizuku.Shell.Command
import app.simple.inure.shizuku.ShizukuUtils
import app.simple.inure.util.ActivityUtils
import app.simple.inure.util.ConditionUtils.isZero
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackersViewModel(application: Application, val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

    var keyword: String = ""
        set(value) {
            field = value
            scanTrackers()
        }

    private var command: String = ""

    private val trackers: MutableLiveData<ArrayList<Any>> by lazy {
        MutableLiveData<ArrayList<Any>>().also {
            scanTrackers()
        }
    }

    private val activityInfo: MutableLiveData<Pair<ActivityInfoModel, Int>> by lazy {
        MutableLiveData<Pair<ActivityInfoModel, Int>>()
    }

    private val serviceInfo: MutableLiveData<Pair<ServiceInfoModel, Int>> by lazy {
        MutableLiveData<Pair<ServiceInfoModel, Int>>()
    }

    fun getTrackers(): LiveData<ArrayList<Any>> {
        return trackers
    }

    fun getActivityInfo(): LiveData<Pair<ActivityInfoModel, Int>> {
        return activityInfo
    }

    fun getServiceInfo(): LiveData<Pair<ServiceInfoModel, Int>> {
        return serviceInfo
    }

    private fun scanTrackers() {
        viewModelScope.launch(Dispatchers.IO) {
            val trackerSignatures = getTrackerSignatures()
            val trackersList = arrayListOf<Any>()

            trackersList.addAll(getActivityTrackers())
            trackersList.addAll(getServicesTrackers())
            trackersList.addAll(getReceiversTrackers())

            Log.d("TrackersViewModel", "scanTrackers: ${trackerSignatures.size}")

            trackersList.sortBy {
                if (it is ActivityInfoModel) {
                    it.name
                } else {
                    (it as ServiceInfoModel).name
                }
            }

            if (trackersList.size.isZero()) {
                postWarning(getString(R.string.no_trackers_found))
            }

            trackers.postValue(trackersList)
        }
    }

    private fun getActivityTrackers(): ArrayList<ActivityInfoModel> {
        val trackerSignatures = getTrackerSignatures()
        val activities = packageManager.getPackageInfo(packageInfo.packageName)!!.activities
        val trackersList = arrayListOf<ActivityInfoModel>()

        if (activities != null) {
            for (activity in activities) {
                for (signature in trackerSignatures) {
                    if (activity.name.lowercase().contains(keyword.lowercase()) || signature.lowercase().contains(keyword.lowercase())) {
                        if (activity.name.contains(signature)) {
                            val activityInfoModel = ActivityInfoModel()

                            activityInfoModel.activityInfo = activity
                            activityInfoModel.name = activity.name
                            activityInfoModel.target = activity.targetActivity ?: getString(R.string.not_available)
                            activityInfoModel.isEnabled = ActivityUtils.isEnabled(applicationContext(), packageInfo.packageName, activity.name)
                            activityInfoModel.trackerId = signature
                            activityInfoModel.isActivity = true

                            trackersList.add(activityInfoModel)

                            break
                        }
                    }
                }
            }
        }

        return trackersList
    }

    private fun getServicesTrackers(): ArrayList<ServiceInfoModel> {
        val trackerSignatures = getTrackerSignatures()
        val services = packageManager.getPackageInfo(packageInfo.packageName)!!.services
        val trackersList = arrayListOf<ServiceInfoModel>()

        if (services != null) {
            for (service in services) {
                for (signature in trackerSignatures) {
                    if (service.name.lowercase().contains(keyword.lowercase()) || signature.lowercase().contains(keyword.lowercase())) {
                        if (service.name.contains(signature)) {
                            val activityInfoModel = ServiceInfoModel()

                            activityInfoModel.serviceInfo = service
                            activityInfoModel.name = service.name
                            activityInfoModel.isEnabled = ActivityUtils.isEnabled(applicationContext(), packageInfo.packageName, service.name)
                            activityInfoModel.trackerId = signature

                            trackersList.add(activityInfoModel)

                            break
                        }
                    }
                }
            }
        }

        return trackersList
    }

    private fun getReceiversTrackers(): ArrayList<ActivityInfoModel> {
        val trackerSignatures = getTrackerSignatures()
        val receivers = packageManager.getPackageInfo(packageInfo.packageName)!!.receivers
        val trackersList = arrayListOf<ActivityInfoModel>()

        if (receivers != null) {
            for (receiver in receivers) {
                for (signature in trackerSignatures) {
                    if (receiver.name.lowercase().contains(keyword.lowercase()) || signature.lowercase().contains(keyword.lowercase())) {
                        if (receiver.name.contains(signature)) {
                            val activityInfoModel = ActivityInfoModel()

                            activityInfoModel.activityInfo = receiver
                            activityInfoModel.name = receiver.name
                            activityInfoModel.isEnabled = ActivityUtils.isEnabled(applicationContext(), packageInfo.packageName, receiver.name)
                            activityInfoModel.trackerId = signature
                            activityInfoModel.isReceiver = true

                            trackersList.add(activityInfoModel)

                            break
                        }
                    }
                }
            }
        }

        return trackersList
    }

    private fun getTrackerSignatures(): Array<out String> {
        return applicationContext().resources.getStringArray(R.array.trackers)
    }

    fun updateTrackersStatus(activityInfoModel: ActivityInfoModel, enabled: Boolean, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (ConfigurationPreferences.isUsingRoot()) {
                Shell.cmd("pm ${if (enabled) "enable" else "disable"} ${packageInfo.packageName}/${activityInfoModel.name}").exec().let {
                    if (it.isSuccess) {
                        activityInfoModel.isEnabled = enabled
                        activityInfo.postValue(Pair(activityInfoModel, position))
                    } else {
                        postWarning(getString(R.string.failed))
                        // Dont change the status
                        activityInfo.postValue(Pair(activityInfoModel, position))
                    }
                }
            } else {
                if (ConfigurationPreferences.isUsingShizuku() && DevelopmentPreferences.get(DevelopmentPreferences.shizukuTrackerBlocker)) {
                    kotlin.runCatching {
                        ShizukuUtils.execInternal(Command("pm ${if (enabled) "enable" else "disable"} ${packageInfo.packageName}/${activityInfoModel.name}"), null).let {
                            if (it.isSuccessful) {
                                activityInfoModel.isEnabled = enabled
                                activityInfo.postValue(Pair(activityInfoModel, position))
                            } else {
                                postWarning(getString(R.string.failed))
                                // Dont change the status
                                activityInfo.postValue(Pair(activityInfoModel, position))
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateTrackersStatus(serviceInfoModel: ServiceInfoModel, enabled: Boolean, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (ConfigurationPreferences.isUsingRoot()) {
                Shell.cmd("pm ${if (enabled) "enable" else "disable"} ${packageInfo.packageName}/${serviceInfoModel.name}").exec().let {
                    if (it.isSuccess) {
                        serviceInfoModel.isEnabled = enabled
                        serviceInfo.postValue(Pair(serviceInfoModel, position))
                    } else {
                        postWarning(getString(R.string.failed))
                        // Dont change the status
                        serviceInfo.postValue(Pair(serviceInfoModel, position))
                    }
                }
            } else {
                if (ConfigurationPreferences.isUsingShizuku() && DevelopmentPreferences.get(DevelopmentPreferences.shizukuTrackerBlocker)) {
                    kotlin.runCatching {
                        ShizukuUtils.execInternal(Command("pm ${if (enabled) "enable" else "disable"} ${packageInfo.packageName}/${serviceInfoModel.name}"), null).let {
                            if (it.isSuccessful) {
                                serviceInfoModel.isEnabled = enabled
                                serviceInfo.postValue(Pair(serviceInfoModel, position))
                            } else {
                                postWarning(getString(R.string.failed))
                                // Dont change the status
                                serviceInfo.postValue(Pair(serviceInfoModel, position))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onShellCreated(shell: Shell?) {

    }

    override fun onShellDenied() {

    }

    override fun onShizukuCreated() {

    }

    fun clear() {
        activityInfo.postValue(null)
        serviceInfo.postValue(null)
    }

    fun enableTrackers(paths: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val stringBuilder = StringBuilder()

            for (path in paths) {
                stringBuilder.append("pm enable ${packageInfo.packageName}/$path && ")
            }

            stringBuilder.append("exit")

            if (ConfigurationPreferences.isUsingRoot()) {
                Shell.cmd(stringBuilder.toString()).exec().let {
                    if (it.isSuccess) {
                        scanTrackers()
                    } else {
                        postWarning(getString(R.string.failed))
                    }
                }
            } else {
                if (ConfigurationPreferences.isUsingShizuku() && DevelopmentPreferences.get(DevelopmentPreferences.shizukuTrackerBlocker)) {
                    kotlin.runCatching {
                        ShizukuUtils.execInternal(Command(stringBuilder.toString()), null).let {
                            Log.d("Shizuku", it.out)
                            Log.d("Shizuku", it.err)

                            scanTrackers()
                        }
                    }.onFailure {
                        postWarning(getString(R.string.failed))
                    }
                }
            }
        }
    }

    fun disableTrackers(paths: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val stringBuilder = StringBuilder()

            for (path in paths) {
                stringBuilder.append("pm disable ${packageInfo.packageName}/$path && ")
            }

            stringBuilder.append("exit")

            if (ConfigurationPreferences.isUsingRoot()) {
                Shell.cmd(stringBuilder.toString()).exec().let {
                    if (it.isSuccess) {
                        scanTrackers()
                    } else {
                        postWarning(getString(R.string.failed))
                    }
                }
            } else {
                if (ConfigurationPreferences.isUsingShizuku() && DevelopmentPreferences.get(DevelopmentPreferences.shizukuTrackerBlocker)) {
                    kotlin.runCatching {
                        ShizukuUtils.execInternal(Command(stringBuilder.toString()), null).let {
                            Log.d("Shizuku", it.out)
                            Log.d("Shizuku", it.err)

                            scanTrackers()
                        }
                    }.onFailure {
                        postWarning(getString(R.string.failed))
                    }
                }
            }
        }
    }
}