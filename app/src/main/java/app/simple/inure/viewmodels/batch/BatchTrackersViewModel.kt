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
import app.simple.inure.apk.utils.ReceiversUtils
import app.simple.inure.apk.utils.ServicesUtils
import app.simple.inure.extensions.viewmodels.RootServiceViewModel
import app.simple.inure.models.Tracker
import app.simple.inure.util.ActivityUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ConditionUtils.isNotNull
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.TrackerUtils.getTrackerSignatures
import com.topjohnwu.superuser.nio.ExtendedFile
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

            packages.forEachIndexed { index, it ->
                progress.postValue("${index + 1}/${packages.size}\n$it")

                val packageInfo = it.getPackageInfo()!!
                trackersList.addAll(packageInfo.getActivityTrackers())
                trackersList.addAll(packageInfo.getServicesTrackers())
                trackersList.addAll(packageInfo.getReceiversTrackers())

                try {
                    readIntentFirewallXml(
                            path.replace(placeHolder, packageInfo.packageName), getFileSystemManager(), trackersList)
                } catch (e: NullPointerException) {
                    Log.e("BatchTrackersViewModel", "Error: ${e.message}")
                }
            }

            //            trackersList.sortBy {
            //                when {
            //                    it.isActivity -> {
            //                        it.activityInfo.packageName
            //                    }
            //                    it.isService -> {
            //                        it.serviceInfo.packageName
            //                    }
            //                    it.isReceiver -> {
            //                        it.activityInfo.packageName
            //                    }
            //                    else -> {
            //                        it.name
            //                    }
            //                }
            //            }

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

    fun changeTrackerState(trackers: ArrayList<Tracker>, shouldBlock: Boolean, function: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            for (packageName in packages) {
                val matchedTrackers = trackers.filter {
                    when {
                        it.isActivity -> {
                            it.activityInfo.packageName == packageName
                        }
                        it.isService -> {
                            it.serviceInfo.packageName == packageName
                        }
                        it.isReceiver -> {
                            it.activityInfo.packageName == packageName
                        }
                        else -> {
                            false
                        }
                    }
                }

                if (shouldBlock) {
                    blockTrackers(path.replace(placeHolder, packageName), packageName, matchedTrackers)
                } else {
                    unblockTrackers(path.replace(placeHolder, packageName), packageName, matchedTrackers)
                }
            }

            withContext(Dispatchers.Main) {
                function()
            }
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
     * @param trackers The list of trackers to be added to the file
     */
    private fun blockTrackers(path: String, packageName: String, trackers: List<Tracker>) {
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

                    if (name == "${packageName}/${tracker.name}") {
                        component.parentNode.removeChild(component)
                    }
                }

                val componentFilter = doc.createElement("component-filter")
                componentFilter.setAttribute("name", "${packageName}/${tracker.name}")

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
        }.getOrElse {
            Log.e("TrackerBlocker", "Error: ${it.message}")
            postWarning("Error: ${it.message}")
        }
    }

    private fun unblockTrackers(path: String, packageName: String, trackers: List<Tracker>) {
        kotlin.runCatching {
            val file: ExtendedFile = getFileSystemManager()!!.getFile(path)

            if (!file.exists()) {
                Log.d("BatchTrackersViewModel", "File does not exist at $path")

                /**
                 * Cancel the process
                 */
                return
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

                    if (name == "${packageName}/${tracker.name}") {
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
        }.getOrElse {
            Log.e("TrackerBlocker", "Error: ${it.message}")
            postWarning("Error: ${it.message}")
        }
    }
}