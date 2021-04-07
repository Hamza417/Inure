package app.simple.inure.util

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.Integer.min
import java.nio.charset.Charset
import java.util.zip.ZipInputStream

object ApkManifestFetcher {
    fun getManifestXmlFromFile(apkFile: File) =
        getManifestXmlFromInputStream(FileInputStream(apkFile))

    fun getManifestXmlFromFilePath(apkFilePath: String) =
        getManifestXmlFromInputStream(FileInputStream(File(apkFilePath)))

    fun getManifestXmlFromInputStream(ApkInputStream: InputStream): String? {
        ZipInputStream(ApkInputStream).use { zipInputStream: ZipInputStream ->
            while (true) {
                val entry = zipInputStream.nextEntry ?: break
                if (entry.name == "AndroidManifest.xml") {
                    return decompressXML(zipInputStream.readBytes())
                }
            }
        }
        return null
    }

    /**
     * Binary XML name space starts
     */
    private const val startNameSpace = 0x00100100

    /**
     * Binary XML name space ends
     */
    private const val endNameSpace = 0x00100101

    /**
     * Binary XML start Tag
     */
    private const val startTag = 0x00100102

    /**
     * Binary XML end Tag
     */
    private const val endTag = 0x00100103

    /**
     * Binary XML text Tag
     */
    private const val textTag = 0x00100104

    /*
     * Flag for UTF-8 encoded file. Default is UTF-16.
     */
    private const val FLAG_UTF_8 = 0x00000100

    /**
     * Reference var for spacing
     * Used in prtIndent()
     */
    private const val spaces = "                                             "

    // Flag if the manifest is in UTF-8 but we don't really handle it.
    private var mIsUTF8 = false

    /**
     * Parse the 'compressed' binary form of Android XML docs
     * such as for AndroidManifest.xml in .apk files
     * Source: http://stackoverflow.com/questions/2097813/how-to-parse-the-androidmanifest-xml-file-inside-an-apk-package/4761689#4761689
     *
     * @param xml Encoded XML content to decompress
     */
    private fun decompressXML(xml: ByteArray): String {
        val resultXml = StringBuilder()
        /*
        Compressed XML file/bytes starts with 24x bytes of data
            9 32 bit words in little endian order (LSB first):
                0th word is 03 00 (Magic number) 08 00 (header size words 0-1)
                1st word is the size of the compressed XML. This should equal size of xml array.
                2nd word is 01 00 (Magic number) 1c 00 (header size words 2-8)
                3rd word is offset of byte after string table
                4th word is number of strings in string table
                5th word is style count
                6th word are flags
                7th word string table offset
                8th word is styles offset
                [string index table (little endian offset into string table)]
                [string table (two byte length followed by text for each entry UTF-16, nul)]
        */

        mIsUTF8 = (lew(xml, 24) and FLAG_UTF_8) != 0

        val numbStrings = lew(xml, 4 * 4)

        // StringIndexTable starts at offset 24x, an array of 32 bit LE offsets
        // of the length/string data in the StringTable.
        val sitOff = 0x24  // Offset of start of StringIndexTable

        // StringTable, each string is represented with a 16 bit little endian
        // character count, followed by that number of 16 bit (LE) (Unicode) chars.
        val stOff = sitOff + numbStrings * 4  // StringTable follows StrIndexTable

        // XMLTags, The XML tag tree starts after some unknown content after the
        // StringTable.  There is some unknown data after the StringTable, scan
        // forward from this point to the flag for the start of an XML start tag.
        var xmlTagOff = lew(xml, 3 * 4)  // Start from the offset in the 3rd word.
        // Scan forward until we find the bytes: 0x02011000(x00100102 in normal int)
        run {
            var ii = xmlTagOff
            while (ii < xml.size - 4) {
                if (lew(xml, ii) == startTag) {
                    xmlTagOff = ii
                    break
                }
                ii += 4
            }
        }

        /*
        XML tags and attributes:

        Every XML start and end tag consists of 6 32 bit words:
            0th word: 02011000 for startTag and 03011000 for endTag
            1st word: a flag?, like 38000000
            2nd word: Line of where this tag appeared in the original source file
            3rd word: 0xFFFFFFFF ??
            4th word: StringIndex of NameSpace name, or 0xFFFFFF for default NS
            5th word: StringIndex of Element Name
            (Note: 01011000 in 0th word means end of XML document, endDocTag)

        Start tags (not end tags) contain 3 more words:
            6th word: 14001400 meaning??
            7th word: Number of Attributes that follow this tag(follow word 8th)
            8th word: 00000000 meaning??

        Attributes consist of 5 words:
            0th word: StringIndex of Attribute Name's Namespace, or 0xFFFFFF
            1st word: StringIndex of Attribute Name
            2nd word: StringIndex of Attribute Value, or 0xFFFFFFF if ResourceId used
            3rd word: Flags?
            4th word: str ind of attr value again, or ResourceId of value

        Text blocks consist of 7 words
            0th word: The text tag (0x00100104)
            1st word: Size of the block (28 bytes)
            2nd word: Line number
            3rd word: 0xFFFFFFFF
            4th word: Index into the string table
            5th word: Unknown
            6th word: Unknown

        startNameSpace blocks consist of 6 words
            0th word: The startNameSpace tag (0x00100100)
            1st word: Size of the block (24 bytes)
            2nd word: Line number
            3rd word: 0xFFFFFFFF
            4th word: Index into the string table for the prefix
            5th word: Index into the string table for the URI

        endNameSpace blocks consist of 6 words
            0th word: The endNameSpace tag (0x00100101)
            1st word: Size of the block (24 bytes)
            2nd word: Line number
            3rd word: 0xFFFFFFFF
            4th word: Index into the string table for the prefix
            5th word: Index into the string table for the URI
        */

        // Step through the XML tree element tags and attributes
        var off = xmlTagOff
        var indent = 0
        while (off < xml.size) {
            val tag0 = lew(xml, off)
            val nameSi = lew(xml, off + 5 * 4)

            when (tag0) {
                startTag -> {
                    val numbAttrs = lew(xml, off + 7 * 4)  // Number of Attributes to follow
                    off += 9 * 4  // Skip over 6+3 words of startTag data
                    val name = compXmlString(xml, sitOff, stOff, nameSi)

                    // Look for the Attributes
                    val sb = StringBuffer()
                    for (ii in 0 until numbAttrs) {
                        val attrNameSi = lew(xml, off + 1 * 4)  // AttrName String Index
                        val attrValueSi = lew(xml, off + 2 * 4) // AttrValue Str Ind, or 0xFFFFFF
                        val attrResId = lew(xml, off + 4 * 4)  // AttrValue ResourceId or dup AttrValue StrInd
                        off += 5 * 4  // Skip over the 5 words of an attribute

                        val attrName = compXmlString(xml, sitOff, stOff, attrNameSi)
                        val attrValue = if (attrValueSi != -1)
                            compXmlString(xml, sitOff, stOff, attrValueSi)
                        else
                            "resourceID 0x" + Integer.toHexString(attrResId)
                        sb.append(" $attrName=\"$attrValue\"")
                    }
                    resultXml.append(prtIndent(indent, "<$name$sb>"))
                    indent++
                }
                endTag -> {
                    indent--
                    off += 6 * 4  // Skip over 6 words of endTag data
                    val name = compXmlString(xml, sitOff, stOff, nameSi)
                    resultXml.append(prtIndent(indent, "</$name>")
                    )

                }
                textTag -> {  // Text that is hanging out between start and end tags
                    val text = compXmlString(xml, sitOff, stOff, lew(xml, off + 16))
                    resultXml.append(text)
                    off += lew(xml, off + 4)
                }
                startNameSpace -> {
                    //Todo startNameSpace and endNameSpace are effectively skipped, but they are not handled.
                    off += lew(xml, off + 4)
                }
                endNameSpace -> {
                    off += lew(xml, off + 4)
                }
                else -> {
                    Log.d(
                        "Applog", "  Unrecognized tag code '" + Integer.toHexString(tag0)
                                + "' at offset " + off
                    )
                }
            }
        }
        return resultXml.toString()
    }

    /**
     * Tool Method for decompressXML();
     * Compute binary XML to its string format
     * Source: Source: http://stackoverflow.com/questions/2097813/how-to-parse-the-androidmanifest-xml-file-inside-an-apk-package/4761689#4761689
     *
     * @param xml Binary-formatted XML
     * @param sitOff
     * @param stOff
     * @param strInd
     * @return String-formatted XML
     */
    private fun compXmlString(
            xml: ByteArray, @Suppress("SameParameterValue") sitOff: Int,
            stOff: Int,
            strInd: Int
    ): String? {
        if (strInd < 0) return null
        val strOff = stOff + lew(xml, sitOff + strInd * 4)
        return compXmlStringAt(xml, strOff)
    }

    /**
     * Tool Method for decompressXML();
     * Apply indentation
     *
     * @param indent Indentation level
     * @param str String to indent
     * @return Indented string
     */
    private fun prtIndent(indent: Int, str: String): String {
        return spaces.substring(0, min(indent * 2, spaces.length)) + str
    }

    /**
     * Tool method for decompressXML()
     * Return the string stored in StringTable format at
     * offset strOff.  This offset points to the 16 bit string length, which
     * is followed by that number of 16 bit (Unicode) chars.
     *
     * @param arr StringTable array
     * @param strOff Offset to get string from
     * @return String from StringTable at offset strOff
     */
    private fun compXmlStringAt(arr: ByteArray, strOff: Int): String {
        var start = strOff
        var charSetUsed: Charset = Charsets.UTF_16LE

        val byteLength = if (mIsUTF8) {
            charSetUsed = Charsets.UTF_8
            start += 2
            arr[strOff + 1].toInt() and 0xFF
        } else { // UTF-16LE
            start += 2
            ((arr[strOff + 1].toInt() and 0xFF shl 8) or (arr[strOff].toInt() and 0xFF)) * 2
        }
        return String(arr, start, byteLength, charSetUsed)
    }

    /**
     * Return value of a Little Endian 32 bit word from the byte array
     * at offset off.
     *
     * @param arr Byte array with 32 bit word
     * @param off Offset to get word from
     * @return Value of Little Endian 32 bit word specified
     */
    private fun lew(arr: ByteArray, off: Int): Int {
        return (arr[off + 3] shl 24 and -0x1000000 or ((arr[off + 2] shl 16) and 0xff0000)
                or (arr[off + 1] shl 8 and 0xff00) or (arr[off].toInt() and 0xFF))
    }

    private infix fun Byte.shl(i: Int): Int = (this.toInt() shl i)
}
