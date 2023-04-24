package app.simple.inure.util

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import app.simple.inure.preferences.AppearancePreferences
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

object XMLUtils {

    private val quotations: Pattern = Pattern.compile("\"([^\"]*)\"", Pattern.MULTILINE)

    @Suppress("RegExpDuplicateAlternationBranch")
    private val tags = Pattern.compile("" /*Only for indentation */ +
                                               "<\\w+\\.+\\S+" + // <xml.yml.zml>
                                               "|<\\w+\\.+\\S+" + // <xml.yml.zml...nthml
                                               "|</\\w+.+>" + // </xml.yml.zml>
                                               "|</\\w+-+\\S+>" + // </xml-yml>
                                               "|<\\w+-+\\S+" + // <xml-yml-zml...nthml
                                               "|</\\w+>" + // </xml>
                                               "|</\\w+" + // </xml
                                               "|<\\w+/>" + // <xml/>
                                               "|<\\w+>" +  // <xml>
                                               "|<\\w+" +  // <xml
                                               "|<.\\w+" + // <?xml
                                               "|\\?>" + // ?>
                                               "|/>", // />
                                       Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

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

    fun String.getPrettyXML(): SpannableString {
        val formattedContent = SpannableString(this)
        val matcher: Matcher = tags.matcher(this)
        while (matcher.find()) {
            formattedContent.setSpan(ForegroundColorSpan(Color.parseColor("#2980B9")), matcher.start(),
                                     matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        matcher.usePattern(quotations)
        while (matcher.find()) {
            formattedContent.setSpan(ForegroundColorSpan(AppearancePreferences.getAccentColor()),
                                     matcher.start(), matcher.end(),
                                     Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return formattedContent
    }

    fun String.formatXML(): String {
        return getProperXml(this) ?: this
    }
}