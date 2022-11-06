package app.simple.inure.apk.xml;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import app.simple.inure.apk.parsers.ResourceTableParser;
import app.simple.inure.apk.structure.resource.ResourceTable;
import app.simple.inure.apk.utils.Inputs;
import app.simple.inure.constants.AndroidConstants;

public class XML implements Closeable {
    
    private final ZipFile zipFile;
    private ResourceTable resourceTable;
    
    public XML(String path) throws IOException {
        File apkFile = new File(path);
        // create zip file cost time, use one zip file for apk parser life cycle
        this.zipFile = new ZipFile(apkFile);
    }
    
    /**
     * trans binary xml file to text xml file.
     *
     * @param path the xml file path in apk file
     * @return the text. null if file not exists
     * @throws IOException if file is invalid
     */
    public String transBinaryXml(String path) throws IOException {
        byte[] data = getFileData(path);
        if (data == null) {
            return null;
        }
        parseResourceTable();
        
        XmlTranslator xmlTranslator = new XmlTranslator();
        transBinaryXml(data, xmlTranslator);
        return xmlTranslator.getXml();
    }
    
    private void transBinaryXml(byte[] data, XmlStreamer xmlStreamer) throws IOException {
        parseResourceTable();
        
        ByteBuffer buffer = ByteBuffer.wrap(data);
        BinaryXmlParser binaryXmlParser = new BinaryXmlParser(buffer, resourceTable);
        binaryXmlParser.setLocale(Locale.US);
        binaryXmlParser.setXmlStreamer(xmlStreamer);
        binaryXmlParser.parse();
    }
    
    /**
     * parse resource table.
     */
    private void parseResourceTable() throws IOException {
        byte[] data = getFileData(AndroidConstants.RESOURCE_FILE);
        Set <Locale> locales;
        if (data == null) {
            // if no resource entry has been found, we assume it is not needed by this APK
            this.resourceTable = new ResourceTable();
            locales = Collections.emptySet();
            return;
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(data);
        ResourceTableParser resourceTableParser = new ResourceTableParser(buffer);
        resourceTableParser.parse();
        this.resourceTable = resourceTableParser.getResourceTable();
        locales = resourceTableParser.getLocales();
    }
    
    public byte[] getFileData(String path) throws IOException {
        ZipEntry entry = zipFile.getEntry(path);
        if (entry == null) {
            return null;
        }
        
        InputStream inputStream = zipFile.getInputStream(entry);
        return Inputs.readAllAndClose(inputStream);
    }
    
    @Override
    public void close() throws IOException {
        zipFile.close();
    }
}
