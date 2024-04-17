package app.simple.inure.viewmodels.installer

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.extensions.viewmodels.RootServiceViewModel
import app.simple.inure.models.Tracker
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.TrackerUtils
import app.simple.inure.util.TrackerUtils.getActivityTrackers
import app.simple.inure.util.TrackerUtils.getReceiverTrackers
import app.simple.inure.util.TrackerUtils.getServiceTrackers
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class InstallerTrackersViewModel(application: Application, private val apkFile: File) : RootServiceViewModel(application) {

    private var path = ""
    private var packageInfo: PackageInfo? = null

    private val flags = PackageManager.GET_ACTIVITIES or
            PackageManager.GET_SERVICES or
            PackageManager.GET_RECEIVERS or
            PackageManager.GET_PROVIDERS

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
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    applicationContext().packageManager.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.PackageInfoFlags.of(flags.toLong()))!!
                } else {
                    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
                    applicationContext().packageManager.getPackageArchiveInfo(apkFile.absolutePath, flags)!!
                }

                if (packageInfo == null) {
                    postWarning("Failed to get package info")
                    return@launch
                }

                if (packageManager.isPackageInstalled(packageInfo?.packageName!!)) {
                    packageInfo = packageManager.getPackageInfo(packageInfo?.packageName!!)!!
                }

                path = "/data/system/ifw/" + "${packageInfo?.packageName}.xml"

                val trackersData = TrackerUtils.getTrackersData()
                val trackersList = arrayListOf<Tracker>()

                trackersList.addAll(packageInfo?.getActivityTrackers(applicationContext(), trackersData)!!)
                trackersList.addAll(packageInfo?.getServiceTrackers(applicationContext(), trackersData)!!)
                trackersList.addAll(packageInfo?.getReceiverTrackers(applicationContext(), trackersData)!!)

                trackersList.sortBy {
                    it.componentName
                }

                if (trackersList.size.isZero()) {
                    postWarning(getString(R.string.no_trackers_found))
                }

                if (ConfigurationPreferences.isUsingRoot()) {
                    TrackerUtils.readIntentFirewallXml(getFileSystemManager()!!, trackersList, path)
                }

                trackers.postValue(trackersList)
            }.getOrElse {
                postError(it)
            }
        }
    }

    override fun runRootProcess(fileSystemManager: FileSystemManager?) {
        scanTrackers()
    }

    fun clear() {
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
                TrackerUtils.blockTrackers(trackers, getFileSystemManager()!!, path, packageInfo!!.packageName)

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
                TrackerUtils.unblockTrackers(trackers, getFileSystemManager()!!, path, packageInfo!!.packageName)

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
        private const val TAG = "InstallerTrackersViewModel"
    }
}
