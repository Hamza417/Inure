package app.simple.inure.apk.parsers

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object ApkManifestFetcher {
    fun getManifestXmlFromInputStream(inputStream: InputStream): String? {
        ZipInputStream(inputStream).use { zipInputStream: ZipInputStream ->
            while (true) {
                val entry = zipInputStream.nextEntry ?: break
                if (entry.name == "AndroidManifest.xml") {
                    //  zip.getInputStream(entry).use { input -> }
                    return decompressXML(zipInputStream.readBytes())
                }
            }
        }
        return null
    }

    fun getManifestXmlFromFile(file: File) = getManifestXmlFromInputStream(FileInputStream(file))

    fun getManifestXmlFromFilePath(filePath: String) =
        getManifestXmlFromInputStream(FileInputStream(File(filePath)))

    fun formatXML(string: String) = decompressXML(string.toByteArray())

    /**
     * Binary XML doc ending Tag
     */
    private var endDocTag = 0x00100101

    /**
     * Binary XML start Tag
     */
    private var startTag = 0x00100102

    /**
     * Binary XML end Tag
     */
    private var endTag = 0x00100103

    /**
     * Reference var for spacing
     * Used in prtIndent()
     */
    private var spaces = "                                             "

    /**
     * Parse the 'compressed' binary form of Android XML docs
     * such as for AndroidManifest.xml in .apk files
     * Source: http://stackoverflow.com/questions/2097813/how-to-parse-the-androidmanifest-xml-file-inside-an-apk-package/4761689#4761689
     *
     * @param xml Encoded XML content to decompress
     */
    private fun decompressXML(xml: ByteArray): String {

        val resultXml = StringBuilder()

        /**
         * Compressed XML file/bytes starts with 24x bytes of data,
         * 9 32 bit words in little endian order (LSB first):
         * 0th word is 03 00 08 00
         * 3rd word SEEMS TO BE:  Offset at then of StringTable
         * 4th word is: Number of strings in string table
         *
         * WARNING: Sometime I indiscriminately display or refer to word in
         * little endian storage format, or in integer format (ie MSB first).
         */
        val numbStrings = lew(xml, 4 * 4)

        /**
         * StringIndexTable starts at offset 24x, an array of 32 bit LE offsets
         * of the length/string data in the StringTable.
         */
        val sitOff = 0x24  // Offset of start of StringIndexTable

        /**
         * StringTable, each string is represented with a 16 bit little endian
         * character count, followed by that number of 16 bit (LE) (Unicode) chars.
         */
        val stOff = sitOff + numbStrings * 4  // StringTable follows StrIndexTable

        /**
         * XMLTags, The XML tag tree starts after some unknown content after the
         * StringTable.  There is some unknown data after the StringTable, scan
         * forward from this point to the flag for the start of an XML start tag.
         */
        var xmlTagOff = lew(xml, 3 * 4)  // Start from the offset in the 3rd word.

        /**
         * Scan forward until we find the bytes: 0x02011000(x00100102 in normal int)
         */
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
        /**
         * end of hack, scanning for start of first start tag
         */

        /**
         * XML tags and attributes:
         * Every XML start and end tag consists of 6 32 bit words:
         * 0th word: 02011000 for startTag and 03011000 for endTag
         * 1st word: a flag?, like 38000000
         * 2nd word: Line of where this tag appeared in the original source file
         * 3rd word: FFFFFFFF ??
         * 4th word: StringIndex of NameSpace name, or FFFFFFFF for default NS
         * 5th word: StringIndex of Element Name
         * (Note: 01011000 in 0th word means end of XML document, endDocTag)
         */

        /**
         * Start tags (not end tags) contain 3 more words:
         * 6th word: 14001400 meaning??
         * 7th word: Number of Attributes that follow this tag(follow word 8th)
         * 8th word: 00000000 meaning??
         */

        /**
         * Attributes consist of 5 words:
         * 0th word: StringIndex of Attribute Name's Namespace, or FFFFFFFF
         * 1st word: StringIndex of Attribute Name
         * 2nd word: StringIndex of Attribute Value, or FFFFFFF if ResourceId used
         * 3rd word: Flags?
         * 4th word: str ind of attr value again, or ResourceId of value
         */

        /**
         * TMP, dump string table to tr for debugging
         * tr.addSelect("strings", null);
         * for (int ii=0; ii<numbStrings; ii++) {
         * Length of string starts at StringTable plus offset in StrIndTable
         * String str = compXmlString(xml, sitOff, stOff, ii);
         * tr.add(String.valueOf(ii), str);
         * }
         */

        /**
         * tr.parent();
         */

        /**
         * Step through the XML tree element tags and attributes
         */
        var off = xmlTagOff
        var indent = 0

        /**
         * var startTagLineNo = -2
         */
        while (off < xml.size) {
            val tag0 = lew(xml, off)

            /**
             * int tag1 = LEW(xml, off+1*4);
             * val lineNo = lew(xml, off + 2 * 4)
             * int tag3 = LEW(xml, off+3*4);
             * val nameNsSi = lew(xml, off + 4 * 4)
             */
            val nameSi = lew(xml, off + 5 * 4)

            // XML START TAG
            if (tag0 == startTag) {
                // val tag6 = lew(xml, off + 6 * 4)  // Expected to be 14001400
                val numbAttrs = lew(xml, off + 7 * 4)  // Number of Attributes to follow
                // int tag8 = LEW(xml, off+8*4);  // Expected to be 00000000
                off += 9 * 4  // Skip over 6+3 words of startTag data
                val name = compXmlString(xml, sitOff, stOff, nameSi)
                // tr.addSelect(name, null);
                // startTagLineNo = lineNo

                // Look for the Attributes
                val sb = StringBuffer()
                for (ii in 0 until numbAttrs) {
                    //                    val attrNameNsSi = lew(xml, off)  // AttrName Namespace Str Ind, or FFFFFFFF
                    val attrNameSi = lew(xml, off + 1 * 4)  // AttrName String Index
                    val attrValueSi = lew(xml, off + 2 * 4) // AttrValue Str Ind, or FFFFFFFF
                    //                    val attrFlags = lew(xml, off + 3 * 4)
                    val attrResId = lew(xml, off + 4 * 4)  // AttrValue ResourceId or dup AttrValue StrInd
                    off += 5 * 4  // Skip over the 5 words of an attribute

                    val attrName = compXmlString(xml, sitOff, stOff, attrNameSi)
                    val attrValue = if (attrValueSi != -1)
                        compXmlString(xml, sitOff, stOff, attrValueSi)
                    else
                        "resourceID 0x" + Integer.toHexString(attrResId)
                    sb.append(" $attrName=\"$attrValue\"")
                    //tr.add(attrName, attrValue);
                }
                resultXml.append(prtIndent(indent, "<$name$sb>"))
                indent++

            } else if (tag0 == endTag) { // XML END TAG
                indent--
                off += 6 * 4  // Skip over 6 words of endTag data
                val name = compXmlString(xml, sitOff, stOff, nameSi)
                resultXml.append(prtIndent(indent, "</$name>")) //  (line $startTagLineNo-$lineNo)
                //tr.parent();  // Step back up the NobTree

            } else if (tag0 == endDocTag) {  // END OF XML DOC TAG
                break

            } else {
                //                println("  Unrecognized tag code '" + Integer.toHexString(tag0)
                //                        + "' at offset " + off
                //                )
                break
            }
        } // end of while loop scanning tags and attributes of XML tree

        // println("end at offset $off")

        return resultXml.toString()
    } // end of decompressXML

    /**
     * Tool Method for decompressXML();
     * Compute binary XML to its string format
     *
     * Source:
     * http://stackoverflow.com/questions/2097813/how-to-parse-the-androidmanifest-xml-file-inside-an-apk-package/4761689#4761689
     *
     * @param xml Binary-formatted XML
     * @param sitOff
     * @param stOff
     * @param strInd
     * @return String-formatted XML
     */
    private fun compXmlString(xml: ByteArray, @Suppress("SameParameterValue") sitOff: Int, stOff: Int, strInd: Int): String? {
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
        return spaces.substring(0, (indent * 2).coerceAtMost(spaces.length)) + str
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
        val strLen = (arr[strOff + 1] shl (8 and 0xff00)) or (arr[strOff].toInt() and 0xff)
        val chars = ByteArray(strLen)
        for (ii in 0 until strLen) {
            chars[ii] = arr[strOff + 2 + ii * 2]
        }
        return String(chars)  // Hack, just use 8 byte chars
    } // end of compXmlStringAt

    /**
     * Return value of a Little Endian 32 bit word from the byte array
     * at offset off.
     *
     * @param arr Byte array with 32 bit word
     * @param off Offset to get word from
     * @return Value of Little Endian 32 bit word specified
     */
    private fun lew(arr: ByteArray, off: Int): Int {
        return arr[off + 3] shl 24 and -0x1000000 or
                (arr[off + 2] shl 16 and 0xff0000) or
                (arr[off + 1] shl 8 and 0xff00) or
                (arr[off].toInt() and 0xFF)
    } // end of LEW

    private infix fun Byte.shl(i: Int): Int = (this.toInt() shl i)
    // private infix fun Int.shl(i: Int): Int = (this shl i)
}