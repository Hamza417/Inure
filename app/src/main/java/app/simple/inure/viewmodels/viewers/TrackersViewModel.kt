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
import app.simple.inure.apk.utils.ReceiversUtils
import app.simple.inure.apk.utils.ServicesUtils
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
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class TrackersViewModel(application: Application, private val packageInfo: PackageInfo) : RootServiceViewModel(application) {

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

    private fun getPackageInfo(): PackageInfo {
        return if (packageManager.isPackageInstalled(packageInfo.packageName)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                packageManager.getPackageInfo(
                        packageInfo.packageName,
                        PackageManager.GET_ACTIVITIES or
                                PackageManager.GET_RECEIVERS or
                                PackageManager.GET_SERVICES or
                                PackageManager.MATCH_DISABLED_COMPONENTS)!!
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                        packageInfo.packageName,
                        PackageManager.GET_ACTIVITIES or
                                PackageManager.GET_RECEIVERS or
                                PackageManager.GET_SERVICES or
                                PackageManager.GET_DISABLED_COMPONENTS)!!
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                packageManager.getPackageArchiveInfo(
                        packageInfo.applicationInfo.sourceDir,
                        PackageManager.GET_ACTIVITIES or
                                PackageManager.GET_RECEIVERS or
                                PackageManager.GET_SERVICES or
                                PackageManager.MATCH_DISABLED_COMPONENTS)!!
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(
                        packageInfo.packageName,
                        PackageManager.GET_ACTIVITIES or
                                PackageManager.GET_RECEIVERS or
                                PackageManager.GET_SERVICES or
                                PackageManager.GET_DISABLED_COMPONENTS)!!
            }
        }
    }

    private fun getActivityTrackers(): ArrayList<Tracker> {
        val trackerSignatures = getTrackerSignatures()
        val activities = getPackageInfo().activities ?: null
        val trackersList = arrayListOf<Tracker>()

        for (signature in trackerSignatures) {
            Log.d("Trackers", signature)
        }

        if (activities != null) {
            for (activity in activities) {
                for (signature in trackerSignatures) {
                    if (activity.name.lowercase().contains(keyword.lowercase()) || signature.lowercase().contains(keyword.lowercase())) {
                        if (activity.name.lowercase().contains(signature.lowercase())) {
                            val tracker = Tracker()

                            tracker.activityInfo = activity
                            tracker.name = activity.name
                            tracker.isEnabled = kotlin.runCatching {
                                ActivityUtils.isEnabled(applicationContext(), packageInfo.packageName, activity.name)
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
        }

        return trackersList
    }

    private fun getServicesTrackers(): ArrayList<Tracker> {
        val trackerSignatures = getTrackerSignatures()
        val services = getPackageInfo().services ?: null
        val trackersList = arrayListOf<Tracker>()

        if (services != null) {
            for (service in services) {
                for (signature in trackerSignatures) {
                    if (service.name.lowercase().contains(keyword.lowercase()) || signature.lowercase().contains(keyword.lowercase())) {
                        if (service.name.lowercase().contains(signature.lowercase())) {
                            val tracker = Tracker()

                            tracker.serviceInfo = service
                            tracker.name = service.name
                            tracker.isEnabled = kotlin.runCatching {
                                ServicesUtils.isEnabled(applicationContext(), packageInfo.packageName, service.name)
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
        }

        return trackersList
    }

    private fun getReceiversTrackers(): ArrayList<Tracker> {
        val trackerSignatures = getTrackerSignatures()
        val receivers = getPackageInfo().receivers ?: null
        val trackersList = arrayListOf<Tracker>()

        if (receivers != null) {
            for (receiver in receivers) {
                for (signature in trackerSignatures) {
                    if (receiver.name.lowercase().contains(keyword.lowercase())
                        || signature.lowercase().contains(keyword.lowercase())) {
                        if (receiver.name.lowercase().contains(signature.lowercase())) {
                            val tracker = Tracker()

                            tracker.activityInfo = receiver
                            tracker.name = receiver.name
                            tracker.isEnabled = kotlin.runCatching {
                                ReceiversUtils.isEnabled(applicationContext(), packageInfo.packageName, receiver.name)
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
        }

        return trackersList
    }

    private fun getTrackerSignatures(): List<String> {
        return applicationContext().resources.getStringArray(R.array.trackers).filter {
            it.isNullOrEmpty().invert()
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
     *      </activity>
     *      <service block="true" log="false">
     *          <component-filter name="package_name/component_name" />
     *      </service>
     * </rules>
     *
     * Parse the file following the above structure
     */
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
                if (trackers.size == 1 && position != -1) {
                    trackers[0].isBlocked = true
                    tracker.postValue(Pair(trackers[0], position))
                } else {
                    scanTrackers()
                }
            }.getOrElse {
                Log.e("TrackerBlocker", "Error: ${it.message}")
                postWarning("Error: ${it.message}")
            }
        }
    }

    fun unblockTrackers(trackers: ArrayList<Tracker>, position: Int = -1) {
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
                if (trackers.size == 1 && position != -1) {
                    trackers[0].isBlocked = false
                    tracker.postValue(Pair(trackers[0], position))
                } else {
                    scanTrackers()
                }
            }.getOrElse {
                Log.e("TrackerBlocker", "Error: ${it.message}")
                postWarning("Error: ${it.message}")
            }
        }
    }
}
