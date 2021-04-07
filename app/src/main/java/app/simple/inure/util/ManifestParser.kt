package app.simple.inure.util

import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class ManifestParser {
    var isSplitApk: Boolean? = null
    var manifestAttributes: HashMap<String, String>? = null

    companion object {
        fun parse(file: File) = parse(java.io.FileInputStream(file))

        fun parse(filePath: String) = parse(File(filePath))

        fun parse(inputStream: InputStream): ManifestParser? {
            val result = ManifestParser()
            val manifestXmlString = ApkManifestFetcher.getManifestXmlFromInputStream(inputStream)
                ?: return null
            val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val builder: DocumentBuilder = factory.newDocumentBuilder()
            val document: Document? = builder.parse(manifestXmlString.byteInputStream())
            if (document != null) {
                document.documentElement.normalize()
                val manifestNode: Node? = document.getElementsByTagName("manifest")?.item(0)
                if (manifestNode != null) {
                    val manifestAttributes = HashMap<String, String>()
                    for (i in 0 until manifestNode.attributes.length) {
                        val node = manifestNode.attributes.item(i)
                        manifestAttributes[node.nodeName] = node.nodeValue
                    }
                    result.manifestAttributes = manifestAttributes
                }
            }
            result.manifestAttributes?.let {
                result.isSplitApk = (it["android:isFeatureSplit"]?.toBoolean()
                    ?: false) || (it.containsKey("split"))
            }
            return result
        }

    }
}