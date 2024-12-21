package app.simple.inure.viewmodels.batch

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
import app.simple.inure.extensions.viewmodels.RootServiceViewModel
import app.simple.inure.models.Tracker
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.TrackerUtils
import app.simple.inure.util.TrackerUtils.getActivityTrackers
import app.simple.inure.util.TrackerUtils.getProviderTrackers
import app.simple.inure.util.TrackerUtils.getReceiverTrackers
import app.simple.inure.util.TrackerUtils.getServiceTrackers
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class BatchTrackersViewModel(application: Application, private val packages: ArrayList<String>) : RootServiceViewModel(application) {

    private val placeHolder = "%1\$s"
    private val path = "/data/system/ifw/$placeHolder.xml"

    private val trackers: MutableLiveData<ArrayList<Tracker>> by lazy {
        MutableLiveData<ArrayList<Tracker>>().also {
            initRootProc()
        }
    }

    private val progress: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.postValue("~/${packages.size}")
        }
    }

    fun getTrackers(): LiveData<ArrayList<Tracker>> {
        return trackers
    }

    fun getProgress(): LiveData<String> {
        return progress
    }

    override fun runRootProcess(fileSystemManager: FileSystemManager?) {
        if (fileSystemManager.isNotNull()) {
            scanTrackers()
        } else {
            postWarning("ERR: Could not acquire file system manager with root access")
        }
    }

    private fun scanTrackers() {
        viewModelScope.launch(Dispatchers.IO) {
            val trackersData = TrackerUtils.getTrackersData()
            val trackersList = arrayListOf<Tracker>()

            packages.forEachIndexed { index, packageName ->
                progress.postValue("${index + 1}/${packages.size}\n$packageName")

                val packageInfo = packageName.getPackageInfo() ?: return@forEachIndexed
                trackersList.addAll(packageInfo.getActivityTrackers(applicationContext(), trackersData))
                trackersList.addAll(packageInfo.getServiceTrackers(applicationContext(), trackersData))
                trackersList.addAll(packageInfo.getReceiverTrackers(applicationContext(), trackersData))
                trackersList.addAll(packageInfo.getProviderTrackers(applicationContext(), trackersData))

                try {
                    TrackerUtils.readIntentFirewallXml(
                            getFileSystemManager()!!, trackersList, path.replace(placeHolder, packageInfo.packageName))
                } catch (e: NullPointerException) {
                    Log.e(TAG, "Error: ${e.message}")
                } catch (e: IOException) {
                    Log.e(TAG, "Error: ${e.message} on $path")
                }
            }

            if (trackersList.size.isZero()) {
                postWarning(getString(R.string.no_trackers_found))
            }

            trackers.postValue(trackersList)
        }
    }

    private fun String.getPackageInfo(): PackageInfo? {
        if (packageManager.isPackageInstalled(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return packageManager.getPackageInfo(
                        this,
                        PackageManager.GET_ACTIVITIES or
                                PackageManager.GET_RECEIVERS or
                                PackageManager.GET_SERVICES or
                                PackageManager.GET_PROVIDERS or
                                PackageManager.MATCH_DISABLED_COMPONENTS or
                                PackageManager.MATCH_UNINSTALLED_PACKAGES)!!
            } else {
                @Suppress("DEPRECATION")
                return packageManager.getPackageInfo(
                        this,
                        PackageManager.GET_ACTIVITIES or
                                PackageManager.GET_RECEIVERS or
                                PackageManager.GET_SERVICES or
                                PackageManager.GET_PROVIDERS or
                                PackageManager.GET_DISABLED_COMPONENTS or
                                PackageManager.GET_UNINSTALLED_PACKAGES)!!
            }
        } else {
            Log.d("BatchTrackersViewModel", "Package not installed")
        }

        return null
    }

    fun changeTrackerState(trackers: ArrayList<Tracker>, isBlock: Boolean, function: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            for (packageName in packages) {
                runCatching {
                    val matchedTrackers = trackers.filter {
                        when {
                            it.isActivity -> {
                                it.activityInfo.packageName == packageName
                            }
                            it.isService -> {
                                it.serviceInfo.packageName == packageName
                            }
                            it.isReceiver -> {
                                it.receiverInfo.packageName == packageName
                            }
                            it.isProvider -> {
                                it.providerInfo.packageName == packageName
                            }
                            else -> {
                                false
                            }
                        }
                    }

                    if (isBlock) {
                        TrackerUtils.blockTrackers(
                                matchedTrackers.toArrayList(),
                                getFileSystemManager()!!,
                                path.replace(placeHolder, packageName),
                                packageName)
                    } else {
                        TrackerUtils.unblockTrackers(
                                matchedTrackers.toArrayList(),
                                getFileSystemManager()!!,
                                path.replace(placeHolder, packageName),
                                packageName)
                    }
                }.getOrElse {
                    /**
                     * Since there can be multiple packages, we don't want to stop the process
                     * This may lead to some packages not being blocked or unblocked
                     */
                    Log.e(TAG, "Error: ${it.message}")
                }
            }

            withContext(Dispatchers.Main) {
                function()
            }
        }
    }

    companion object {
        private const val TAG = "BatchTrackersViewModel"
    }
}
