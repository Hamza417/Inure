package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.extensions.viewmodels.RootServiceViewModel
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.models.ServiceInfoModel
import app.simple.inure.models.Tracker
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ActivityUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ConditionUtils.isZero
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.Charset

class TrackersViewModel(application: Application, val packageInfo: PackageInfo) : RootServiceViewModel(application) {

    var keyword: String = ""
        set(value) {
            field = value
            scanTrackers()
        }

    private val trackers: MutableLiveData<ArrayList<Tracker>> by lazy {
        MutableLiveData<ArrayList<Tracker>>().also {
            if (ConfigurationPreferences.isUsingRoot()) {
                initRootProc()
            } else {
                scanTrackers()
            }
        }
    }

    private val activityInfo: MutableLiveData<Pair<ActivityInfoModel, Int>> by lazy {
        MutableLiveData<Pair<ActivityInfoModel, Int>>()
    }

    private val serviceInfo: MutableLiveData<Pair<ServiceInfoModel, Int>> by lazy {
        MutableLiveData<Pair<ServiceInfoModel, Int>>()
    }

    fun getTrackers(): LiveData<ArrayList<Tracker>> {
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
            val trackersList = arrayListOf<Tracker>()

            trackersList.addAll(getActivityTrackers())
            trackersList.addAll(getServicesTrackers())
            trackersList.addAll(getReceiversTrackers())

            Log.d("TrackersViewModel", "scanTrackers: ${trackerSignatures.size}")

            trackersList.sortBy {
                it.name
            }

            if (trackersList.size.isZero()) {
                postWarning(getString(R.string.no_trackers_found))
            }

            if (ConfigurationPreferences.isUsingRoot()) {
                readIntentFirewallXml(getFileSystemManager(), trackersList)
            }

            trackers.postValue(trackersList)
        }
    }

    private fun getActivityTrackers(): ArrayList<Tracker> {
        val trackerSignatures = getTrackerSignatures()
        val activities = packageManager.getPackageInfo(packageInfo.packageName)!!.activities
        val trackersList = arrayListOf<Tracker>()

        if (activities != null) {
            for (activity in activities) {
                for (signature in trackerSignatures) {
                    if (activity.name.lowercase().contains(keyword.lowercase()) || signature.lowercase().contains(keyword.lowercase())) {
                        if (activity.name.contains(signature)) {
                            val tracker = Tracker()

                            tracker.activityInfo = activity
                            tracker.name = activity.name
                            tracker.isEnabled = ActivityUtils.isEnabled(applicationContext(), packageInfo.packageName, activity.name)
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
        }

        return trackersList
    }

    private fun getServicesTrackers(): ArrayList<Tracker> {
        val trackerSignatures = getTrackerSignatures()
        val services = packageManager.getPackageInfo(packageInfo.packageName)!!.services
        val trackersList = arrayListOf<Tracker>()

        if (services != null) {
            for (service in services) {
                for (signature in trackerSignatures) {
                    if (service.name.lowercase().contains(keyword.lowercase()) || signature.lowercase().contains(keyword.lowercase())) {
                        if (service.name.contains(signature)) {
                            val tracker = Tracker()

                            tracker.serviceInfo = service
                            tracker.name = service.name
                            tracker.isEnabled = ActivityUtils.isEnabled(applicationContext(), packageInfo.packageName, service.name)
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
        }

        return trackersList
    }

    private fun getReceiversTrackers(): ArrayList<Tracker> {
        val trackerSignatures = getTrackerSignatures()
        val receivers = packageManager.getPackageInfo(packageInfo.packageName)!!.receivers
        val trackersList = arrayListOf<Tracker>()

        if (receivers != null) {
            for (receiver in receivers) {
                for (signature in trackerSignatures) {
                    if (receiver.name.lowercase().contains(keyword.lowercase()) || signature.lowercase().contains(keyword.lowercase())) {
                        if (receiver.name.contains(signature)) {
                            val tracker = Tracker()

                            tracker.activityInfo = receiver
                            tracker.name = receiver.name
                            tracker.isEnabled = ActivityUtils.isEnabled(applicationContext(), packageInfo.packageName, receiver.name)
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
        }

        return trackersList
    }

    private fun getTrackerSignatures(): Array<out String> {
        return applicationContext().resources.getStringArray(R.array.trackers)
    }

    override fun runRootProcess(fileSystemManager: FileSystemManager?) {
        scanTrackers()
    }

    fun clear() {
        activityInfo.postValue(null)
        serviceInfo.postValue(null)
    }

    private fun readIntentFirewallXml(fileSystemManager: FileSystemManager?, trackersList: ArrayList<Tracker>) {
        val path = "/data/system/ifw/" + "${packageInfo.packageName}.xml"

        if (fileSystemManager?.getFile(path)?.exists()?.invert()!!) {
            return
        }

        val channel = fileSystemManager.openChannel(path, FileSystemManager.MODE_READ_WRITE)
        val capacity = channel.size().toInt()
        val buffer = ByteBuffer.allocate(capacity)
        channel.read(buffer)
        buffer.flip()

        val xml = String(buffer.array(), Charset.defaultCharset())

        val xmlParser = XmlPullParserFactory.newInstance().newPullParser()

        xmlParser.apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false)
            setInput(StringReader(xml))
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

        var eventType = xmlParser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                Log.d("TrackerBlocker", "Tag name: ${xmlParser.name}")

                when (xmlParser.name) {
                    "activity" -> {
                        val block = xmlParser.getAttributeValue(null, "block")
                        Log.d("TrackerBlocker", "Block: $block")

                        // Get the next tag
                        eventType = xmlParser.next()

                        while (eventType != XmlPullParser.END_TAG) {
                            if (eventType == XmlPullParser.START_TAG) {
                                if (xmlParser.name == "component-filter") {
                                    val componentName = xmlParser.getAttributeValue(null, "name")
                                    Log.d("TrackerBlocker", "Component name: $componentName")

                                    if (componentName != null) {
                                        for (tracker in trackersList) {
                                            if (tracker.name == componentName.split("/")[1]) {
                                                tracker.isBlocked = block == "true"
                                                Log.d("TrackerBlocker", "Tracker: ${tracker.name} - ${tracker.isBlocked}")
                                            }
                                        }
                                    }
                                }
                            }

                            eventType = xmlParser.next()
                        }
                    }
                    "service" -> {
                        val block = xmlParser.getAttributeValue(null, "block")
                        Log.d("TrackerBlocker", "Block: $block")

                        // Get the next tag
                        eventType = xmlParser.next()

                        while (eventType != XmlPullParser.END_TAG) {
                            if (eventType == XmlPullParser.START_TAG) {
                                if (xmlParser.name == "component-filter") {
                                    val componentName = xmlParser.getAttributeValue(null, "name")
                                    Log.d("TrackerBlocker", "Component name: $componentName")

                                    if (componentName != null) {
                                        for (tracker in trackersList) {
                                            if (tracker.name == componentName.split("/")[1]) {
                                                tracker.isBlocked = block == "true"
                                                Log.d("TrackerBlocker", "Tracker: ${tracker.name} - ${tracker.isBlocked}")
                                            }
                                        }
                                    }
                                }
                            }

                            eventType = xmlParser.next()
                        }
                    }
                    "broadcast" -> {
                        val block = xmlParser.getAttributeValue(null, "block")
                        Log.d("TrackerBlocker", "Block: $block")

                        // Get the next tag
                        eventType = xmlParser.next()

                        while (eventType != XmlPullParser.END_TAG) {
                            if (eventType == XmlPullParser.START_TAG) {
                                if (xmlParser.name == "component-filter") {
                                    val componentName = xmlParser.getAttributeValue(null, "name")
                                    Log.d("TrackerBlocker", "Component name: $componentName")

                                    if (componentName != null) {
                                        for (tracker in trackersList) {
                                            if (tracker.name == componentName.split("/")[1]) {
                                                tracker.isBlocked = block == "true"
                                                Log.d("TrackerBlocker", "Tracker: ${tracker.name} - ${tracker.isBlocked}")
                                            }
                                        }
                                    }
                                }
                            }

                            eventType = xmlParser.next()
                        }
                    }
                }
            }

            eventType = xmlParser.next()
        }
    }
}