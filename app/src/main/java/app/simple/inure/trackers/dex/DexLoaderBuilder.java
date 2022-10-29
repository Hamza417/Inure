package app.simple.inure.trackers.dex;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import app.simple.inure.util.IOUtils;
import dalvik.system.DexClassLoader;

public class DexLoaderBuilder {
    
    private static final int BUF_SIZE = 8 * 1024;
    
    private DexLoaderBuilder() {
    
    }
    
    @SuppressWarnings ("unused")
    public static DexClassLoader fromFile(Context context, final File dexFile) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(dexFile);
        byte[] bFile = IOUtils.toByteArray(fileInputStream);
        return fromBytes(context, bFile);
    }
    
    public static DexClassLoader fromBytes(Context context, final byte[] dexBytes) {
        
        if (null == context) {
            throw new RuntimeException("No context provided");
        }
        
        String dexFileName = "internal.dex";
        
        final File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE), dexFileName);
        
        if (!dexInternalStoragePath.exists()) {
            prepareDex(dexBytes, dexInternalStoragePath);
        }
        
        final File optimizedDexOutputPath = context.getCodeCacheDir();
        
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) optimizedDexOutputPath = context.getCodeCacheDir(); else
        
        DexClassLoader loader = new DexClassLoader(dexInternalStoragePath.getAbsolutePath(),
                optimizedDexOutputPath.getAbsolutePath(), null, context.getClassLoader().getParent());
        
        //noinspection ResultOfMethodCallIgnored
        dexInternalStoragePath.delete();
        //noinspection ResultOfMethodCallIgnored
        optimizedDexOutputPath.delete();
        
        return loader;
    }
    
    @SuppressWarnings ("UnusedReturnValue")
    private static boolean prepareDex(byte[] bytes, File dexInternalStoragePath) {
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;
        
        try {
            bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
            dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while ((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
            return true;
        } catch (IOException e) {
            if (dexWriter != null) {
                try {
                    dexWriter.close();
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
            return false;
        }
    }
}