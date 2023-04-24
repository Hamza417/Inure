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
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ActivityUtils
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

    private val trackers: MutableLiveData<ArrayList<Any>> by lazy {
        MutableLiveData<ArrayList<Any>>().also {
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

            if (ConfigurationPreferences.isUsingRoot()) {
                readIntentFirewallXml(getFileSystemManager(), trackersList)
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
                            val serviceInfoModel = ServiceInfoModel()

                            serviceInfoModel.serviceInfo = service
                            serviceInfoModel.name = service.name
                            serviceInfoModel.isEnabled = ActivityUtils.isEnabled(applicationContext(), packageInfo.packageName, service.name)
                            serviceInfoModel.trackerId = signature

                            trackersList.add(serviceInfoModel)

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

    override fun runRootProcess(fileSystemManager: FileSystemManager?) {
        scanTrackers()
    }

    fun clear() {
        activityInfo.postValue(null)
        serviceInfo.postValue(null)
    }

    private fun readIntentFirewallXml(fileSystemManager: FileSystemManager?, trackersList: ArrayList<Any>) {
        val path = "/data/system/ifw/" + "${packageInfo.packageName}.xml"
        val channel = fileSystemManager?.openChannel(path, FileSystemManager.MODE_READ_WRITE)
        val capacity = channel?.size()?.toInt() ?: 0
        val buffer = ByteBuffer.allocate(capacity)
        channel?.read(buffer)
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
                                        for (tracker in trackersList.filterIsInstance<ActivityInfoModel>()) {
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
                                        for (tracker in trackersList.filterIsInstance<ServiceInfoModel>()) {
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
                                        for (tracker in trackersList.filterIsInstance<ActivityInfoModel>()) {
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