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
import app.simple.inure.models.Tracker
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ActivityUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ConditionUtils.isZero
import com.topjohnwu.superuser.nio.ExtendedFile
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class TrackersViewModel(application: Application, val packageInfo: PackageInfo) : RootServiceViewModel(application) {

    var keyword: String = ""
        set(value) {
            field = value
            scanTrackers()
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

    fun getTrackers(): LiveData<ArrayList<Tracker>> {
        return trackers
    }

    private fun scanTrackers() {
        viewModelScope.launch(Dispatchers.IO) {
            val trackerSignatures = getTrackerSignatures()
            val trackersList = arrayListOf<Tracker>()

            trackersList.addAll(getActivityTrackers())
            trackersList.addAll(getServicesTrackers())
            trackersList.addAll(getReceiversTrackers())

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

    }

    private fun readIntentFirewallXml(fileSystemManager: FileSystemManager?, trackersList: ArrayList<Tracker>) {
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
     * I think writing directly to string is the best way to go about this
     *
     * @param trackers The list of trackers to be added to the file
     */
    fun blockTrackers(trackers: ArrayList<Tracker>) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val file: ExtendedFile = getFileSystemManager()!!.getFile(path)

                if (!file.exists()) {
                    file.newOutputStream().use {
                        it.write("<rules>\n</rules>".toByteArray())
                    }
                }

                val channel = getFileSystemManager()!!.openChannel(path, FileSystemManager.MODE_READ_WRITE)
                val capacity = channel.size().toInt()
                val buffer = ByteBuffer.allocate(capacity)
                channel.read(buffer)
                buffer.flip()

                val xml = String(buffer.array(), Charset.defaultCharset())

                val docFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                val docBuilder: DocumentBuilder = docFactory.newDocumentBuilder()
                val doc: Document = docBuilder.parse(InputSource(StringReader(xml)))

                // Modify the XML document
                val rules = doc.getElementsByTagName("rules").item(0)

                for (tracker in trackers) {
                    val components = doc.getElementsByTagName("component-filter")

                    /**
                     * Remove the component if it already exists
                     * This is to prevent duplicate entries
                     */
                    for (i in 0 until components.length) {
                        val component = components.item(i)
                        val name = component.attributes.getNamedItem("name").nodeValue

                        if (name == "${packageInfo.packageName}/${tracker.name}") {
                            component.parentNode.removeChild(component)
                        }
                    }

                    val componentFilter = doc.createElement("component-filter")
                    componentFilter.setAttribute("name", "${packageInfo.packageName}/${tracker.name}")

                    if (tracker.isActivity) {
                        // Check if the activity tag exists
                        val activity = doc.getElementsByTagName("activity").item(0)

                        if (activity == null) {
                            val activity1 = doc.createElement("activity")
                            activity1.setAttribute("block", "true")
                            activity1.setAttribute("log", "false")
                            activity1.appendChild(componentFilter)

                            rules.appendChild(activity1)
                        } else {
                            /**
                             * Check if block already exists and is true, if false
                             * create another activity tag with block and log attributes
                             * set to true
                             */
                            if (activity.attributes.getNamedItem("block") != null && activity.attributes.getNamedItem("block").nodeValue == "false") {
                                val activity1 = doc.createElement("activity")
                                activity1.setAttribute("block", "true")
                                activity1.setAttribute("log", "false")
                                activity1.appendChild(componentFilter)

                                rules.appendChild(activity1)
                            } else {
                                activity.appendChild(componentFilter)
                            }
                        }
                    }

                    if (tracker.isService) {
                        // Check if the service tag exists
                        val service = doc.getElementsByTagName("service").item(0)

                        if (service == null) {
                            val service1 = doc.createElement("service")
                            service1.setAttribute("block", "true")
                            service1.setAttribute("log", "false")
                            service1.appendChild(componentFilter)

                            rules.appendChild(service1)
                        } else {
                            /**
                             * Check if block already exists and is true, if false
                             * create another service tag with block and log attributes
                             * set to true
                             */
                            if (service.attributes.getNamedItem("block") != null && service.attributes.getNamedItem("block").nodeValue == "false") {
                                val service1 = doc.createElement("service")
                                service1.setAttribute("block", "true")
                                service1.setAttribute("log", "false")
                                service1.appendChild(componentFilter)

                                rules.appendChild(service1)
                            } else {
                                service.appendChild(componentFilter)
                            }
                        }
                    }

                    if (tracker.isReceiver) {
                        // Check if the broadcast tag exists
                        val broadcast = doc.getElementsByTagName("broadcast").item(0)

                        if (broadcast == null) {
                            val broadcast1 = doc.createElement("broadcast")
                            broadcast1.setAttribute("block", "true")
                            broadcast1.setAttribute("log", "false")
                            broadcast1.appendChild(componentFilter)

                            rules.appendChild(broadcast1)
                        } else {
                            /**
                             * Check if block already exists and is true, if false
                             * create another broadcast tag with block and log attributes
                             * set to true
                             */
                            if (broadcast.attributes.getNamedItem("block") != null && broadcast.attributes.getNamedItem("block").nodeValue == "false") {
                                val broadcast1 = doc.createElement("broadcast")
                                broadcast1.setAttribute("block", "true")
                                broadcast1.setAttribute("log", "false")
                                broadcast1.appendChild(componentFilter)

                                rules.appendChild(broadcast1)
                            } else {
                                broadcast.appendChild(componentFilter)
                            }
                        }
                    }
                }

                // Write the XML document back to the file
                val transformerFactory: TransformerFactory = TransformerFactory.newInstance()
                val transformer: Transformer = transformerFactory.newTransformer()
                val source = DOMSource(doc)

                channel.truncate(0)

                val outputStream = file.newOutputStream()
                val result = StreamResult(outputStream)
                transformer.transform(source, result)

                channel.close()

                // Update the trackers list
                scanTrackers()
            }.getOrElse {
                Log.e("TrackerBlocker", "Error: ${it.message}")
                postWarning("Error: ${it.message}")
            }
        }
    }

    fun enableTrackers(trackers: java.util.ArrayList<Tracker>) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val file: ExtendedFile = getFileSystemManager()!!.getFile(path)

                if (!file.exists()) {
                    postWarning(getString(R.string.no_rules_file_found))

                    /**
                     * Cancel the process
                     */
                    return@launch
                }

                val channel = getFileSystemManager()!!.openChannel(path, FileSystemManager.MODE_READ_WRITE)
                val capacity = channel.size().toInt()
                val buffer = ByteBuffer.allocate(capacity)
                channel.read(buffer)
                buffer.flip()

                val xml = String(buffer.array(), Charset.defaultCharset())

                val docFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                val docBuilder: DocumentBuilder = docFactory.newDocumentBuilder()
                val doc: Document = docBuilder.parse(InputSource(StringReader(xml)))

                // Modify the XML document
                // val rules = doc.getElementsByTagName("rules").item(0)

                for (tracker in trackers) {
                    val components = doc.getElementsByTagName("component-filter")

                    /**
                     * Remove the component if it already exists
                     * This is to prevent duplicate entries
                     */
                    for (i in 0 until components.length) {
                        val component = components.item(i)
                        val name = component.attributes.getNamedItem("name").nodeValue

                        if (name == "${packageInfo.packageName}/${tracker.name}") {
                            component.parentNode.removeChild(component)
                        }
                    }
                }

                // Write the XML document back to the file
                val transformerFactory: TransformerFactory = TransformerFactory.newInstance()
                val transformer: Transformer = transformerFactory.newTransformer()
                val source = DOMSource(doc)

                channel.truncate(0)

                val outputStream = file.newOutputStream()
                val result = StreamResult(outputStream)
                transformer.transform(source, result)

                channel.close()

                // Update the trackers list
                scanTrackers()
            }.getOrElse {
                Log.e("TrackerBlocker", "Error: ${it.message}")
                postWarning("Error: ${it.message}")
            }
        }
    }
}