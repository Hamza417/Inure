package app.simple.inure.apk.dex;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import androidx.annotation.Nullable;
import app.simple.inure.apk.utils.Inputs;
import app.simple.inure.constants.AndroidConstants;
import app.simple.inure.exceptions.ParserException;

public class Dex implements Closeable {
    
    private DexClass[] dexClasses;
    private final ZipFile zipFile;
    private final File apkFile;
    @Nullable
    private FileChannel fileChannel;
    
    public Dex(String path) throws IOException {
        this.apkFile = new File(path);
        this.zipFile = new ZipFile(apkFile);
    }
    
    /**
     * get class information form dex file. currently only class name
     */
    public DexClass[] getDexClasses() throws IOException {
        if (this.dexClasses == null) {
            parseDexFiles();
        }
        return this.dexClasses;
    }
    
    private void parseDexFiles() throws IOException {
        dexClasses = parseDexFile(AndroidConstants.DEX_FILE);
        for (int i = 2; i < 1000; i++) {
            String path = String.format(Locale.US, AndroidConstants.DEX_ADDITIONAL, i);
            try {
                DexClass[] classes = parseDexFile(path);
                dexClasses = mergeDexClasses(dexClasses, classes);
            } catch (ParserException e) {
                break;
            }
        }
    }
    
    private DexClass[] parseDexFile(String path) throws IOException {
        byte[] data = getFileData(path);
        if (data == null) {
            String msg = String.format("Dex file %s not found", path);
            throw new ParserException(msg);
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        DexParser dexParser = new DexParser(buffer);
        return dexParser.parse();
    }
    
    private DexClass[] mergeDexClasses(DexClass[] first, DexClass[] second) {
        DexClass[] result = new DexClass[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
    
    public byte[] getFileData(String path) throws IOException {
        ZipEntry entry = zipFile.getEntry(path);
        if (entry == null) {
            return null;
        }
        
        InputStream inputStream = zipFile.getInputStream(entry);
        return Inputs.readAllAndClose(inputStream);
    }
    
    protected ByteBuffer fileData() throws IOException {
        fileChannel = new FileInputStream(apkFile).getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
    }
    
    @Override
    public void close() throws IOException {
        zipFile.close();
        if (fileChannel != null) {
            fileChannel.close();
        }
    }
}
