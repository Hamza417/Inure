package app.simple.inure.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import app.simple.inure.apk.utils.ReceiversUtils
import app.simple.inure.apk.utils.ServicesUtils
import app.simple.inure.models.Tracker
import app.simple.inure.util.ArrayUtils.toStringArray
import app.simple.inure.util.ConditionUtils.invert
import com.topjohnwu.superuser.nio.ExtendedFile
import com.topjohnwu.superuser.nio.FileSystemManager
import org.json.JSONObject
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object TrackerUtils {

    private const val TRACKERS_JSON = "/trackers.json"
    private const val TAG = "TrackersUtils"

    fun getTrackerSignatures(): List<String> {
        ProcessUtils.ensureNotOnMainThread {
            val bufferedReader = BufferedReader(InputStreamReader(
                    TrackerUtils::class.java.getResourceAsStream(TRACKERS_JSON)))
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            val json = stringBuilder.toString()
            val jsonObject = JSONObject(json)
            val trackers = jsonObject.getJSONObject("trackers")
            val signatures = arrayListOf<String>()

            val keysIterator = trackers.keys()

            while (keysIterator.hasNext()) {
                val key = keysIterator.next()
                val tracker = trackers.getJSONObject(key)
                val codeSignature = tracker.getString("code_signature")

                codeSignature.split("|").forEach {
                    if (it.isNotEmpty()) {
                        signatures.add(it)
                    }
                }
            }

            return signatures
        }
    }

    /**
     * {
     *     "trackers": {
     *         "1": {
     *             "categories": [
     *                 "Analytics"
     *             ],
     *             "code_signature": "com.databerries.|com.geolocstation.",
     *             "creation_date": "2017-09-24",
     *             "description": "",
     *             "documentation": [],
     *             "id": 1,
     *             "name": "Teemo",
     *             "network_signature": "databerries\\.com",
     *             "website": "https://www.teemo.co"
     *         },
     *     }
     *  }
     */
    fun getTrackersData(): ArrayList<Tracker> {
        ProcessUtils.ensureNotOnMainThread {
            val bufferedReader = BufferedReader(InputStreamReader(
                    TrackerUtils::class.java.getResourceAsStream(TRACKERS_JSON)))
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            val json = stringBuilder.toString()
            val jsonObject = JSONObject(json)
            val trackers = jsonObject.getJSONObject("trackers")
            val trackersList = arrayListOf<Tracker>()

            val keysIterator = trackers.keys()

            while (keysIterator.hasNext()) {
                val key = keysIterator.next()
                val tracker = trackers.getJSONObject(key)
                val name = tracker.getString("name")
                val codeSignature = tracker.getString("code_signature")
                val networkSignature = tracker.getString("network_signature")
                val website = tracker.getString("website")
                val creationDate = tracker.getString("creation_date")
                val description = tracker.getString("description")
                val categories = tracker.getJSONArray("categories")
                val documentation = tracker.getJSONArray("documentation")

                val trackerObject = Tracker()
                trackerObject.name = name
                trackerObject.codeSignature = codeSignature
                trackerObject.networkSignature = networkSignature
                trackerObject.website = website
                trackerObject.creationDate = creationDate
                trackerObject.description = description
                trackerObject.categories = categories.toStringArray()
                trackerObject.documentation = documentation.toStringArray()

                trackersList.add(trackerObject)
            }

            return trackersList.filter {
                it.codeSignature.isNotEmpty()
            } as ArrayList<Tracker>
        }
    }

    fun PackageInfo.getActivityTrackers(context: Context, trackersData: ArrayList<Tracker>, keyword: String = ""): ArrayList<Tracker> {
        val activities = activities ?: null
        val trackersList = arrayListOf<Tracker>()

        if (activities != null) {
            for (activity in activities) {
                for (tracker in trackersData) {
                    tracker.codeSignature.split("|").forEach {
                        if (activity.name.lowercase().contains(keyword.lowercase()) || it.lowercase().contains(keyword.lowercase())) {
                            if (activity.name.lowercase().contains(it.lowercase())) {
                                val tracker1 = Tracker()
                                tracker.copyBasicTrackerInfo(tracker1)

                                tracker1.activityInfo = activity
                                tracker1.componentName = activity.name
                                tracker1.isEnabled = kotlin.runCatching {
                                    ActivityUtils.isEnabled(context, packageName, activity.name)
                                }.getOrElse {
                                    false
                                }
                                tracker1.isReceiver = false
                                tracker1.isService = false
                                tracker1.isActivity = true

                                trackersList.add(tracker1)

                                return@forEach
                            }
                        }
                    }
                }
            }
        }

        return trackersList
    }

    fun PackageInfo.getServiceTrackers(context: Context, trackersData: ArrayList<Tracker>, keyword: String = ""): ArrayList<Tracker> {
        val services = services ?: null
        val trackersList = arrayListOf<Tracker>()

        if (services != null) {
            for (service in services) {
                for (tracker in trackersData) {
                    tracker.codeSignature.split("|").forEach {
                        if (service.name.lowercase().contains(keyword.lowercase()) || it.lowercase().contains(keyword.lowercase())) {
                            if (service.name.lowercase().contains(it.lowercase())) {
                                val tracker1 = Tracker()
                                tracker.copyBasicTrackerInfo(tracker1)

                                tracker1.serviceInfo = service
                                tracker1.componentName = service.name
                                tracker1.isEnabled = kotlin.runCatching {
                                    ServicesUtils.isEnabled(context, packageName, service.name)
                                }.getOrElse {
                                    false
                                }
                                tracker1.isReceiver = false
                                tracker1.isService = true
                                tracker1.isActivity = false

                                trackersList.add(tracker1)

                                return@forEach
                            }
                        }
                    }
                }
            }
        }

        return trackersList
    }

    fun PackageInfo.getReceiverTrackers(context: Context, trackersData: ArrayList<Tracker>, keyword: String = ""): ArrayList<Tracker> {
        val receivers = receivers ?: null
        val trackersList = arrayListOf<Tracker>()

        if (receivers != null) {
            for (receiver in receivers) {
                for (tracker in trackersData) {
                    tracker.codeSignature.split("|").forEach {
                        if (receiver.name.lowercase().contains(keyword.lowercase()) || it.lowercase().contains(keyword.lowercase())) {
                            if (receiver.name.lowercase().contains(it.lowercase())) {
                                val tracker1 = Tracker()
                                tracker.copyBasicTrackerInfo(tracker1)

                                tracker1.receiverInfo = receiver
                                tracker1.componentName = receiver.name
                                tracker1.isEnabled = kotlin.runCatching {
                                    ReceiversUtils.isEnabled(context, packageName, receiver.name)
                                }.getOrElse {
                                    false
                                }
                                tracker1.isReceiver = true
                                tracker1.isService = false
                                tracker1.isActivity = false

                                trackersList.add(tracker1)

                                return@forEach
                            }
                        }
                    }
                }
            }
        }

        return trackersList
    }

    fun PackageInfo.hasTrackers(trackersData: ArrayList<Tracker>): Boolean {
        val activities = activities ?: null
        val services = services ?: null
        val receivers = receivers ?: null

        if (activities != null) {
            for (activity in activities) {
                for (tracker in trackersData) {
                    tracker.codeSignature.split("|").forEach {
                        if (activity.name.contains(it)) {
                            return true
                        }
                    }
                }
            }
        }

        if (services != null) {
            for (service in services) {
                for (tracker in trackersData) {
                    tracker.codeSignature.split("|").forEach {
                        if (service.name.contains(it)) {
                            return true
                        }
                    }
                }
            }
        }

        if (receivers != null) {
            for (receiver in receivers) {
                for (tracker in trackersData) {
                    tracker.codeSignature.split("|").forEach {
                        if (receiver.name.contains(it)) {
                            return true
                        }
                    }
                }
            }
        }

        return false
    }

    fun PackageInfo.getComponentsPackageInfo(context: Context): PackageInfo {
        val packageManager = context.packageManager
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.MATCH_DISABLED_COMPONENTS or
                    PackageManager.MATCH_UNINSTALLED_PACKAGES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_DISABLED_COMPONENTS or
                    PackageManager.GET_UNINSTALLED_PACKAGES
        }

        return packageManager.getPackageInfo(packageName, flags)
    }

    fun readIntentFirewallXml(fileSystemManager: FileSystemManager, trackersList: ArrayList<Tracker>, path: String) {
        with(fileSystemManager.getFile(path)) {
            if (this.exists()) {
                // Read and check if file has the valid structure

            } else {
                checkAndCreateIFWDirectory(fileSystemManager, path)
                this.newOutputStream().use {
                    it.write("<rules>\n</rules>".toByteArray())
                }
            }
        }

        val channel = fileSystemManager.openChannel(path, FileSystemManager.MODE_READ_WRITE)
        val capacity = channel.size().toInt()
        val buffer = ByteBuffer.allocate(capacity)
        channel.read(buffer)
        buffer.flip()

        var xml = String(buffer.array(), Charset.defaultCharset())

        if (isValidXMLStructure(xml).invert()) {
            Log.d(TAG, "Invalid XML structure for path: $path")
            fileSystemManager.getFile(path).newOutputStream().use {
                // Empty the file
                it.write("<rules>\n</rules>".toByteArray())
                Log.d(TAG, "Created new XML structure for path: $path")
                xml = "<rules>\n</rules>"
            }
        }

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

                        trackersList.find { it.componentName == componentName.split("/")[1] }?.let {
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

                        trackersList.find { it.componentName == componentName.split("/")[1] }?.let {
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

                        trackersList.find { it.componentName == componentName.split("/")[1] }?.let {
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
    fun blockTrackers(trackers: ArrayList<Tracker>, fileSystemManager: FileSystemManager, path: String, packageName: String) {
        val file: ExtendedFile = fileSystemManager.getFile(path)

        with(file) {
            if (this.exists()) {
                // Read and check if file has the valid structure

            } else {
                checkAndCreateIFWDirectory(fileSystemManager, path)
                this.newOutputStream().use {
                    it.write("<rules>\n</rules>".toByteArray())
                }
            }
        }

        val channel = fileSystemManager.openChannel(path, FileSystemManager.MODE_READ_WRITE)
        val capacity = channel.size().toInt()
        val buffer = ByteBuffer.allocate(capacity)
        channel.read(buffer)
        buffer.flip()

        var xml = String(buffer.array(), Charset.defaultCharset())

        if (isValidXMLStructure(xml).invert()) {
            fileSystemManager.getFile(path).newOutputStream().use {
                // Empty the file
                it.write("<rules>\n</rules>".toByteArray())
                xml = "<rules>\n</rules>"
            }
        }

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

                if (name == "${packageName}/${tracker.componentName}") {
                    component.parentNode.removeChild(component)
                }
            }

            val componentFilter = doc.createElement("component-filter")
            componentFilter.setAttribute("name", "${packageName}/${tracker.componentName}")

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
                    if (activity.attributes.getNamedItem("block") != null
                            && activity.attributes.getNamedItem("block").nodeValue == "false") {
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
                    if (service.attributes.getNamedItem("block") != null
                            && service.attributes.getNamedItem("block").nodeValue == "false") {
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
                    if (broadcast.attributes.getNamedItem("block") != null
                            && broadcast.attributes.getNamedItem("block").nodeValue == "false") {
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
    }

    fun unblockTrackers(trackers: ArrayList<Tracker>, fileSystemManager: FileSystemManager, path: String, packageName: String) {
        val file: ExtendedFile = fileSystemManager.getFile(path)

        if (!file.exists()) {
            throw FileNotFoundException()
        }

        val channel = fileSystemManager.openChannel(path, FileSystemManager.MODE_READ_WRITE)
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

                if (name == "${packageName}/${tracker.componentName}") {
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
    }

    private fun isValidXMLStructure(xml: String): Boolean {
        try {
            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc = docBuilder.parse(InputSource(StringReader(xml)))
            val root = doc.documentElement

            // Check if root element is "rules"
            if (root.nodeName != "rules") {
                return false
            }

            // Check if all child elements are either "activity", "service", or "broadcast"
            val children = root.childNodes
            for (i in 0 until children.length) {
                val child = children.item(i)
                if (child.nodeType == Node.ELEMENT_NODE
                        && child.nodeName != "activity" && child.nodeName != "service" && child.nodeName != "broadcast") {
                    return false
                }
            }

            return true
        } catch (e: SAXParseException) {
            return false
        }
    }

    private fun checkAndCreateIFWDirectory(fileSystemManager: FileSystemManager, path: String) {
        val file = fileSystemManager.getFile(path.substringBeforeLast("/") + "/")
        if (!file.exists()) {
            file.mkdirs()
        }
    }
}
