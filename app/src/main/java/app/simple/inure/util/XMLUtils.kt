package app.simple.inure.util

import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

object XMLUtils {

    /**
     * Format xml file to correct indentation ...
     */
    fun getProperXml(dirtyXml: String): String? {
        return try {
            val document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(InputSource(ByteArrayInputStream(dirtyXml.toByteArray(charset("utf-8")))))
            val xPath = XPathFactory.newInstance().newXPath()
            val nodeList = xPath.evaluate("//text()[normalize-space()='']",
                                          document,
                                          XPathConstants.NODESET) as NodeList
            for (i in 0 until nodeList.length) {
                val node = nodeList.item(i)
                node.parentNode.removeChild(node)
            }
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            val stringWriter = StringWriter()
            val streamResult = StreamResult(stringWriter)
            transformer.transform(DOMSource(document), streamResult)
            stringWriter.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun String.formatXML(): String {
        return getProperXml(this) ?: this
    }
}