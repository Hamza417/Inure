package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.extensions.viewmodels.RootServiceViewModel
import app.simple.inure.models.Tracker
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.TrackerUtils
import app.simple.inure.util.TrackerUtils.getActivityTrackers
import app.simple.inure.util.TrackerUtils.getProviderTrackers
import app.simple.inure.util.TrackerUtils.getReceiverTrackers
import app.simple.inure.util.TrackerUtils.getServiceTrackers
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class TrackersViewModel(application: Application, private val packageInfo: PackageInfo) : RootServiceViewModel(application) {

    private var searchJob: Job? = null
    private var trackersList: ArrayList<Tracker> = arrayListOf()

    var keyword: String = ""
        set(value) {
            field = value
            filterTrackers(value)
        }

    private val path = "/data/system/ifw/" + "${packageInfo.packageName}.xml"

    private val trackers: MutableLiveData<ArrayList<Tracker>> by lazy {
        MutableLiveData<ArrayList<Tracker>>().also {
            if (ConfigurationPreferences.isUsingRoot()) {
                initRootProc()
            } else {
                scanTrackers()
            }
        }
    }

    private val tracker: MutableLiveData<Pair<Tracker, Int>> by lazy {
        MutableLiveData<Pair<Tracker, Int>>()
    }

    fun getTrackers(): LiveData<ArrayList<Tracker>> {
        return trackers
    }

    fun getTracker(): LiveData<Pair<Tracker, Int>> {
        return tracker
    }

    private fun scanTrackers() {
        searchJob?.cancel(CancellationException("New search initiated"))

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val packageInfo = getPackageInfo()
                val trackersData = TrackerUtils.getTrackersData()

                ensureActive()
                trackersList.addAll(packageInfo.getActivityTrackers(applicationContext(), trackersData, keyword))
                ensureActive()
                trackersList.addAll(packageInfo.getServiceTrackers(applicationContext(), trackersData, keyword))
                ensureActive()
                trackersList.addAll(packageInfo.getReceiverTrackers(applicationContext(), trackersData, keyword))
                ensureActive()
                trackersList.addAll(packageInfo.getProviderTrackers(applicationContext(), trackersData, keyword))

                trackersList.sortBy {
                    it.componentName
                }

                ensureActive()
                if (trackersList.size.isZero()) {
                    if (keyword.isEmpty()) {
                        postWarning(getString(R.string.no_trackers_found))
                    }
                }

                if (ConfigurationPreferences.isUsingRoot()) {
                    TrackerUtils.readIntentFirewallXml(getFileSystemManager()!!, trackersList, path)
                }

                ensureActive()
                trackers.postValue(trackersList)
            }.onFailure {
                it.printStackTrace()
                if (it !is CancellationException) {
                    postWarning("Error: ${it.message ?: "Unknown error"}")
                }
            }
        }
    }

    private fun filterTrackers(keyword: String) {
        if (keyword.isEmpty()) return trackers.postValue(trackersList)

        val filteredList = arrayListOf<Tracker>()
        for (tracker in trackersList) {
            if (tracker.componentName.contains(keyword, ignoreCase = true)) {
                filteredList.add(tracker)
            }
        }

        trackers.postValue(filteredList)
    }

    private fun getPackageInfo(): PackageInfo {
        return if (packageManager.isPackageInstalled(packageInfo.packageName)) {
            packageManager.getPackageInfo(packageInfo.packageName, COMPONENT_FLAGS)
        } else {
            packageManager.getPackageArchiveInfo(packageInfo.safeApplicationInfo.sourceDir, COMPONENT_FLAGS)!!
        }
    }

    override fun runRootProcess(fileSystemManager: FileSystemManager?) {
        if (fileSystemManager.isNotNull()) {
            fileSystemManager?.let {
                scanTrackers()
            }
        } else {
            postWarning("ERR: Could not acquire file system manager with root access")
        }
    }

    fun clearTrackersList() {
        tracker.value = null
    }

    /**
     * <rules>
     *      <activity block="true" log="false">
     *          <component-filter name="package_name/component_name" />
     *          ...
     *      </activity>
     *      <service block="true" log="false">
     *          <component-filter name="package_name/component_name" />
     *          ...
     *      </service>
     *      <broadcast block="true" log="false">
     *          <component-filter name="package_name/component_name" />
     *          ...
     *      </broadcast>
     * </rules>
     *
     * Parse the file following the above structure and append the components
     * into subsequent tags (activity, service, broadcast), if the tags don't
     * exist, create them.
     *
     * @param trackers The list of trackers to be added to the file
     */
    fun blockTrackers(trackers: ArrayList<Tracker>, position: Int = -1) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                TrackerUtils.blockTrackers(trackers, getFileSystemManager()!!, path, packageInfo.packageName)

                // Update the trackers list
                if (trackers.size == 1 && position != -1) {
                    trackers[0].isBlocked = true
                    tracker.postValue(Pair(trackers[0], position))
                } else {
                    scanTrackers()
                }
            }.getOrElse {
                Log.e(TAG, "Error: ${it.message}")
                postWarning("Error: ${it.message}")
            }
        }
    }

    fun unblockTrackers(trackers: ArrayList<Tracker>, position: Int = -1) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                TrackerUtils.unblockTrackers(trackers, getFileSystemManager()!!, path, packageInfo.packageName)

                // Update the trackers list
                if (trackers.size == 1 && position != -1) {
                    trackers[0].isBlocked = false
                    tracker.postValue(Pair(trackers[0], position))
                } else {
                    scanTrackers()
                }
            }.getOrElse {
                Log.e(TAG, "Error: ${it.message}")
                postWarning("Error: ${it.message}")
            }
        }
    }

    companion object {
        private const val TAG = "TrackersViewModel"
        private val COMPONENT_FLAGS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_PROVIDERS or
                    PackageManager.MATCH_DISABLED_COMPONENTS
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_PROVIDERS or
                    PackageManager.GET_DISABLED_COMPONENTS
        }
    }
}
