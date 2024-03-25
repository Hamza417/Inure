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
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.TrackerUtils
import app.simple.inure.util.TrackerUtils.getActivityTrackers
import app.simple.inure.util.TrackerUtils.getReceiverTrackers
import app.simple.inure.util.TrackerUtils.getServiceTrackers
import com.topjohnwu.superuser.nio.ExtendedFile
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
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
            val trackersData = TrackerUtils.getTrackersData()
            val trackersList = arrayListOf<Tracker>()

            packages.forEachIndexed { index, packageName ->
                progress.postValue("${index + 1}/${packages.size}\n$packageName")

                val packageInfo = packageName.getPackageInfo() ?: return@forEachIndexed
                trackersList.addAll(packageInfo.getActivityTrackers(applicationContext(), trackersData))
                trackersList.addAll(packageInfo.getServiceTrackers(applicationContext(), trackersData))
                trackersList.addAll(packageInfo.getReceiverTrackers(applicationContext(), trackersData))

                try {
                    TrackerUtils.readIntentFirewallXml(
                            getFileSystemManager()!!, trackersList, path.replace(placeHolder, packageInfo.packageName))
                } catch (e: NullPointerException) {
                    Log.e("BatchTrackersViewModel", "Error: ${e.message}")
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
                                PackageManager.MATCH_DISABLED_COMPONENTS or
                                PackageManager.MATCH_UNINSTALLED_PACKAGES)!!
            } else {
                @Suppress("DEPRECATION")
                return packageManager.getPackageInfo(
                        this,
                        PackageManager.GET_ACTIVITIES or
                                PackageManager.GET_RECEIVERS or
                                PackageManager.GET_SERVICES or
                                PackageManager.GET_DISABLED_COMPONENTS or
                                PackageManager.GET_UNINSTALLED_PACKAGES)!!
            }
        } else {
            Log.d("BatchTrackersViewModel", "Package not installed")
        }

        return null
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
                            it.receiverInfo.packageName == packageName
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

            val xml = String(buffer.array(), StandardCharsets.UTF_8)

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
        }.getOrElse {
            Log.e("TrackerBlocker", "Error: ${it.message}")
            postError(it)
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

            val xml = String(buffer.array(), StandardCharsets.UTF_8)

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
        }.getOrElse {
            Log.e("TrackerBlocker", "Error: ${it.message}")
            postError(it)
        }
    }
}
