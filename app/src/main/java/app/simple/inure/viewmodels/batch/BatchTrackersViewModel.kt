package app.simple.inure.viewmodels.batch

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.ReceiversUtils
import app.simple.inure.apk.utils.ServicesUtils
import app.simple.inure.extensions.viewmodels.RootServiceViewModel
import app.simple.inure.models.Tracker
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ActivityUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ConditionUtils.isNotNull
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.TrackerUtils.getTrackerSignatures
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import javax.xml.parsers.DocumentBuilderFactory

class BatchTrackersViewModel(application: Application, private val packages: ArrayList<String>) : RootServiceViewModel(application) {

    private val path = "/data/system/ifw/" + "%package_name%.xml"

    private val trackers: MutableLiveData<ArrayList<Tracker>> by lazy {
        MutableLiveData<ArrayList<Tracker>>().also {
            if (ConfigurationPreferences.isUsingRoot()) {
                initRootProc()
            } else {
                Log.d("BatchTrackersViewModel", "Not using root")
            }
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

    private fun scanTrackers() {
        viewModelScope.launch(Dispatchers.IO) {
            val trackersList = arrayListOf<Tracker>()

            packages.forEach {
                val packageInfo = it.getPackageInfo()!!
                trackersList.addAll(packageInfo.getActivityTrackers())
                trackersList.addAll(packageInfo.getServicesTrackers())
                trackersList.addAll(packageInfo.getReceiversTrackers())

                if (ConfigurationPreferences.isUsingRoot()) {
                    readIntentFirewallXml(path.replace("%package_name%", packageInfo.packageName), getFileSystemManager(), trackersList)
                }
            }

            trackersList.sortBy {
                it.name.substringAfterLast(".")
            }

            if (trackersList.size.isZero()) {
                postWarning(getString(R.string.no_trackers_found))
            }

            trackers.postValue(trackersList)
        }
    }

    private fun PackageInfo.getActivityTrackers(): ArrayList<Tracker> {
        val trackerSignatures = applicationContext().getTrackerSignatures()
        val activities = activities ?: null
        val trackersList = arrayListOf<Tracker>()

        if (activities != null) {
            for (activity in activities) {
                for (signature in trackerSignatures) {
                    if (activity.name.lowercase().contains(signature.lowercase())) {
                        val tracker = Tracker()

                        tracker.activityInfo = activity
                        tracker.name = activity.name
                        tracker.isEnabled = kotlin.runCatching {
                            ActivityUtils.isEnabled(applicationContext(), packageName, activity.name)
                        }.getOrElse {
                            false
                        }

                        tracker.trackerId = signature
                        tracker.isReceiver = false
                        tracker.isService = false
                        tracker.isActivity = true

                        trackersList.add(tracker)

                        break
                    }
                }
            }
        }

        return trackersList
    }

    private fun PackageInfo.getServicesTrackers(): ArrayList<Tracker> {
        val trackerSignatures = applicationContext().getTrackerSignatures()
        val services = services ?: null
        val trackersList = arrayListOf<Tracker>()

        if (services != null) {
            for (service in services) {
                for (signature in trackerSignatures) {
                    if (service.name.lowercase().contains(signature.lowercase())) {
                        val tracker = Tracker()

                        tracker.serviceInfo = service
                        tracker.name = service.name
                        tracker.isEnabled = kotlin.runCatching {
                            ServicesUtils.isEnabled(applicationContext(), packageName, service.name)
                        }.getOrElse {
                            false
                        }
                        tracker.trackerId = signature
                        tracker.isReceiver = false
                        tracker.isService = true
                        tracker.isActivity = false

                        trackersList.add(tracker)

                        break
                    }
                }
            }
        }

        return trackersList
    }

    private fun PackageInfo.getReceiversTrackers(): ArrayList<Tracker> {
        val trackerSignatures = applicationContext().getTrackerSignatures()
        val receivers = receivers ?: null
        val trackersList = arrayListOf<Tracker>()

        if (receivers != null) {
            for (receiver in receivers) {
                for (signature in trackerSignatures) {
                    if (receiver.name.lowercase().contains(signature.lowercase())) {
                        val tracker = Tracker()

                        tracker.activityInfo = receiver
                        tracker.name = receiver.name
                        tracker.isEnabled = kotlin.runCatching {
                            ReceiversUtils.isEnabled(applicationContext(), packageName, receiver.name)
                        }.getOrElse {
                            false
                        }
                        tracker.trackerId = signature
                        tracker.isReceiver = true
                        tracker.isService = false
                        tracker.isActivity = false

                        trackersList.add(tracker)

                        break
                    }
                }
            }
        }

        return trackersList
    }

    private fun String.getPackageInfo(): PackageInfo? {
        if (packageManager.isPackageInstalled(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return packageManager.getPackageInfo(
                        this,
                        PackageManager.GET_ACTIVITIES or
                                PackageManager.GET_RECEIVERS or
                                PackageManager.GET_SERVICES or
                                PackageManager.MATCH_DISABLED_COMPONENTS)!!
            } else {
                @Suppress("DEPRECATION")
                return packageManager.getPackageInfo(
                        this,
                        PackageManager.GET_ACTIVITIES or
                                PackageManager.GET_RECEIVERS or
                                PackageManager.GET_SERVICES or
                                PackageManager.GET_DISABLED_COMPONENTS)!!
            }
        } else {
            Log.d("BatchTrackersViewModel", "Package not installed")
        }

        return null
    }

    /**
     * <rules>
     *      <activity block="true" log="false">
     *          <component-filter name="package_name/component_name" />
     *      </activity>
     *      <service block="true" log="false">
     *          <component-filter name="package_name/component_name" />
     *      </service>
     * </rules>
     *
     * Parse the file following the above structure
     */
    private fun readIntentFirewallXml(path: String, fileSystemManager: FileSystemManager?, trackersList: ArrayList<Tracker>) {
        if (fileSystemManager?.getFile(path)?.exists()?.invert()!!) {
            return
        }

        val channel = fileSystemManager.openChannel(path, FileSystemManager.MODE_READ_WRITE)
        val capacity = channel.size().toInt()
        val buffer = ByteBuffer.allocate(capacity)
        channel.read(buffer)
        buffer.flip()

        val xml = String(buffer.array(), Charset.defaultCharset())
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(xml)))

        val activityNodes = document.getElementsByTagName("activity")
        val serviceNodes = document.getElementsByTagName("service")
        val broadcastNodes = document.getElementsByTagName("broadcast")

        for (i in 0 until activityNodes.length) {
            val activityNode: Node = activityNodes.item(i)
            if (activityNode.nodeType == Node.ELEMENT_NODE) {
                val activityElement = activityNode as Element
                val isBlocked = activityElement.getAttribute("block").toBoolean()
                val componentFilters: NodeList = activityElement.getElementsByTagName("component-filter")
                for (j in 0 until componentFilters.length) {
                    val componentFilterNode: Node = componentFilters.item(j)
                    if (componentFilterNode.nodeType == Node.ELEMENT_NODE) {
                        val componentFilterElement = componentFilterNode as Element
                        val componentName = componentFilterElement.getAttribute("name")

                        trackersList.find { it.name == componentName.split("/")[1] }?.let {
                            it.isBlocked = isBlocked
                        }
                    }
                }
            }
        }

        for (i in 0 until serviceNodes.length) {
            val serviceNode: Node = serviceNodes.item(i)
            if (serviceNode.nodeType == Node.ELEMENT_NODE) {
                val serviceElement = serviceNode as Element
                val isBlocked = serviceElement.getAttribute("block").toBoolean()
                val componentFilters: NodeList = serviceElement.getElementsByTagName("component-filter")
                for (j in 0 until componentFilters.length) {
                    val componentFilterNode: Node = componentFilters.item(j)
                    if (componentFilterNode.nodeType == Node.ELEMENT_NODE) {
                        val componentFilterElement = componentFilterNode as Element
                        val componentName = componentFilterElement.getAttribute("name")

                        trackersList.find { it.name == componentName.split("/")[1] }?.let {
                            it.isBlocked = isBlocked
                        }
                    }
                }
            }
        }

        for (i in 0 until broadcastNodes.length) {
            val broadcastNode: Node = broadcastNodes.item(i)
            if (broadcastNode.nodeType == Node.ELEMENT_NODE) {
                val broadcastElement = broadcastNode as Element
                val isBlocked = broadcastElement.getAttribute("block").toBoolean()
                val componentFilters: NodeList = broadcastElement.getElementsByTagName("component-filter")
                for (j in 0 until componentFilters.length) {
                    val componentFilterNode: Node = componentFilters.item(j)
                    if (componentFilterNode.nodeType == Node.ELEMENT_NODE) {
                        val componentFilterElement = componentFilterNode as Element
                        val componentName = componentFilterElement.getAttribute("name")

                        trackersList.find { it.name == componentName.split("/")[1] }?.let {
                            it.isBlocked = isBlocked
                        }
                    }
                }
            }
        }

        channel.close()
    }
}