// SPDX-License-Identifier: GPL-3.0-or-later

package app.simple.inure.apk.decoders;

import android.os.Build;
import android.text.TextUtils;

import com.reandroid.apk.AndroidFrameworks;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlPullParser;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.xml.XMLDocument;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import androidx.annotation.NonNull;
import app.simple.inure.util.IntegerUtils;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

public class XMLDecoder {
    
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 50;
    private final ZipFile zipFile;
    
    public XMLDecoder(ZipFile zipFile) {
        this.zipFile = zipFile;
    }
    
    public XMLDecoder(String path) throws IOException {
        this.zipFile = new ZipFile(path);
    }
    
    public XMLDecoder(File file) throws IOException {
        this.zipFile = new ZipFile(file);
    }
    
    public String decode(String path) throws IOException {
        byte[] data = getFileData(path);
        if (data == null) {
            return null;
        }
        if (isBinaryXml(ByteBuffer.wrap(data))) {
            return decode(data);
        } else {
            return new String(data);
        }
    }
    
    private byte[] getFileData(String path) throws IOException {
        ZipFile zipFile = this.zipFile;
        if (zipFile == null) {
            return null;
        }
        ZipEntry entry = zipFile.getEntry(path);
        if (entry == null) {
            return null;
        }
        
        try (InputStream is = zipFile.getInputStream(entry)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
            int n;
            while (-1 != (n = is.read(buf))) {
                buffer.write(buf, 0, n);
            }
            return buffer.toByteArray();
        }
    }
    
    public boolean isBinaryXml(@NonNull ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.mark();
        int version = IntegerUtils.getUInt16(buffer);
        int header = IntegerUtils.getUInt16(buffer);
        buffer.reset();
        // 0x0000 is NULL header. The only example of application using a NULL header is NP Manager
        return (version == 0x0003 || version == 0x0000) && header == 0x0008;
    }
    
    @NonNull
    public String decode(@NonNull byte[] data) throws IOException {
        if (isBinaryXml(ByteBuffer.wrap(data))) {
            return decode(ByteBuffer.wrap(data));
        } else {
            return new String(data);
        }
    }
    
    @NonNull
    public String decode(@NonNull InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        int n;
        while (-1 != (n = is.read(buf))) {
            buffer.write(buf, 0, n);
        }
        return decode(buffer.toByteArray());
    }
    
    @NonNull
    public String decode(@NonNull ByteBuffer byteBuffer) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            decode(byteBuffer, bos);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return bos.toString(StandardCharsets.UTF_8);
            } else {
                //noinspection CharsetObjectCanBeUsed
                return bos.toString("UTF-8");
            }
        }
    }
    
    public static void decode(@NonNull ByteBuffer byteBuffer, @NonNull OutputStream os) throws IOException {
        try (BlockReader reader = new BlockReader(byteBuffer.array());
             PrintStream out = new PrintStream(os)) {
            ResXmlDocument resXmlDocument = new ResXmlDocument();
            resXmlDocument.readBytes(reader);
            try (ResXmlPullParser parser = new ResXmlPullParser()) {
                parser.setCurrentPackage(getFrameworkPackageBlock());
                parser.setResXmlDocument(resXmlDocument);
                StringBuilder indent = new StringBuilder(10);
                final String indentStep = "  ";
                out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                XML_BUILDER:
                while (true) {
                    int type = parser.next();
                    switch (type) {
                        case START_TAG: {
                            out.printf("%s<%s%s", indent, getNamespacePrefix(parser.getPrefix()), parser.getName());
                            indent.append(indentStep);
                            
                            int nsStart = parser.getNamespaceCount(parser.getDepth() - 1);
                            int nsEnd = parser.getNamespaceCount(parser.getDepth());
                            for (int i = nsStart; i < nsEnd; ++i) {
                                out.printf("\n%sxmlns:%s=\"%s\"", indent,
                                        parser.getNamespacePrefix(i),
                                        parser.getNamespaceUri(i));
                            }
                            
                            for (int i = 0; i != parser.getAttributeCount(); ++i) {
                                out.printf("\n%s%s%s=\"%s\"",
                                        indent,
                                        getNamespacePrefix(parser.getAttributePrefix(i)),
                                        parser.getAttributeName(i),
                                        parser.getAttributeValue(i));
                            }
                            out.println(">");
                            break;
                        }
                        case END_TAG: {
                            indent.setLength(indent.length() - indentStep.length());
                            out.printf("%s</%s%s>%n", indent, getNamespacePrefix(parser.getPrefix()), parser.getName());
                            break;
                        }
                        case END_DOCUMENT:
                            break XML_BUILDER;
                        case START_DOCUMENT:
                            // Unreachable statement
                            break;
                    }
                }
            }
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }
    
    public static XMLDocument decodeToXml(@NonNull ByteBuffer byteBuffer) throws IOException {
        ResXmlDocument xmlBlock = new ResXmlDocument();
        try (BlockReader reader = new BlockReader(byteBuffer.array())) {
            xmlBlock.readBytes(reader);
            xmlBlock.setPackageBlock(getFrameworkPackageBlock());
            return xmlBlock.decodeToXml();
        }
    }
    
    @NonNull
    private static String getNamespacePrefix(String prefix) {
        if (TextUtils.isEmpty(prefix)) {
            return "";
        }
        return prefix + ":";
    }
    
    @NonNull
    static PackageBlock getFrameworkPackageBlock() throws IOException {
        if (frameworkPackageBlock != null) {
            return frameworkPackageBlock;
        }
        frameworkPackageBlock = AndroidFrameworks.getLatest().getTableBlock().getAllPackages().next();
        return frameworkPackageBlock;
    }
    
    private static PackageBlock frameworkPackageBlock;
}