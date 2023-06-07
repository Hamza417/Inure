package app.simple.inure.shizuku;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.IBinder;
import android.system.Os;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import androidx.annotation.Nullable;
import app.simple.inure.BuildConfig;
import app.simple.inure.preferences.ShellPreferences;
import app.simple.inure.util.IOUtils;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.ShizukuRemoteProcess;
import rikka.shizuku.SystemServiceHelper;

public class ShizukuUtils {
    
    private static final String TAG = "ShizukuUtils";
    private static final String rishPath = "/data/local/tmp/rish";
    
    @SuppressLint ("PrivateApi")
    public static void setAppDisabled(boolean disabled, Set <String> pkgNames)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        /*
         * Call android.content.pm.IPackageManager.setApplicationEnabledSetting with reflection.
         * Through Shizuku wrapper.
         * References:
         *             - https://www.xda-developers.com/implementing-shizuku/
         *             - https://github.dev/aistra0528/Hail
         */
        Log.d("ShizukuHider", "setAppDisabled: " + disabled);
        Method setApplicationEnabledSetting;
        Object iPmInstance;
        
        Class <?> iPmClass = Class.forName("android.content.pm.IPackageManager");
        Class <?> iPmStub = Class.forName("android.content.pm.IPackageManager$Stub");
        Method asInterfaceMethod = iPmStub.getMethod("asInterface", IBinder.class);
        iPmInstance = asInterfaceMethod.invoke(null, new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));
    
        setApplicationEnabledSetting = iPmClass.getMethod("setApplicationEnabledSetting", String.class, int.class, int.class, int.class, String.class);
    
        for (String packageName : pkgNames) {
            setApplicationEnabledSetting.invoke(iPmInstance, packageName,
                    disabled ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER : PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, 0, Os.getuid() / 100000,
                    BuildConfig.APPLICATION_ID);
            Log.i("ShizukuHider", "Hid app: " + packageName);
        }
    }
    
    @SuppressLint ("PrivateApi")
    public static void setAppHidden(boolean hidden, Set <String> pkgNames)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        /*
         * Call android.content.pm.IPackageManager.setApplicationHiddenSetting with reflection.
         * Through Shizuku wrapper.
         * References:
         *             - https://www.xda-developers.com/implementing-shizuku/
         *             - https://github.dev/aistra0528/Hail
         */
        Log.d("ShizukuHider", "setAppHidden: " + hidden);
        Method setApplicationHiddenSetting;
        Object iPmInstance;
    
        Class <?> iPmClass = Class.forName("android.content.pm.IPackageManager");
        Class <?> iPmStub = Class.forName("android.content.pm.IPackageManager$Stub");
        Method asInterfaceMethod = iPmStub.getMethod("asInterface", IBinder.class);
        iPmInstance = asInterfaceMethod.invoke(null, new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));
    
        setApplicationHiddenSetting = iPmClass.getMethod("setApplicationHiddenSettingAsUser", String.class, boolean.class, int.class, int.class);
    
        for (String packageName : pkgNames) {
            setApplicationHiddenSetting.invoke(iPmInstance, packageName, hidden, 0, Os.getuid() / 100000);
            Log.i("ShizukuHider", "Hid app: " + packageName);
        }
    }
    
    @SuppressLint ("PrivateApi")
    public static void clearAppCache(Set <String> pkgNames) {
        /*
         * Call android.content.pm.IPackageManager.deleteApplicationCacheFiles with reflection.
         * Through Shizuku wrapper.
         * References:
         *             - https://www.xda-developers.com/implementing-shizuku/
         *             - https://github.dev/aistra0528/Hail
         */
        Log.d("ShizukuHider", "clearAppCache");
        Method deleteApplicationCacheFiles;
        Object iPmInstance;
        
        try {
            Class <?> iPmClass = Class.forName("android.content.pm.IPackageManager");
            Class <?> iPmStub = Class.forName("android.content.pm.IPackageManager$Stub");
            Method asInterfaceMethod = iPmStub.getMethod("asInterface", IBinder.class);
            iPmInstance = asInterfaceMethod.invoke(null, new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));
            
            deleteApplicationCacheFiles = iPmClass.getMethod("deleteApplicationCacheFiles", String.class, IPackageDataObserver.class);
            
            for (String packageName : pkgNames) {
                deleteApplicationCacheFiles.invoke(iPmInstance, packageName, new IPackageDataObserver.Stub() {
                    @Override
                    public void onRemoveCompleted(String s, boolean b) {
                        Log.i("ShizukuHider", "Cleared app cache: " + s);
                    }
                });
                Log.i("ShizukuHider", "Cleared app cache: " + packageName);
            }
        } catch (Exception e) {
            Log.e("ShizukuHider", "clearAppCache: ", e);
        }
    }
    
    @SuppressLint ("PrivateApi")
    public static void updateComponentState(String packageName, String componentName, boolean enabled)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        /*
         * Call android.content.pm.IPackageManager.setComponentEnabledSetting with reflection.
         * Through Shizuku wrapper.
         * References:
         *             - https://www.xda-developers.com/implementing-shizuku/
         *             - https://github.dev/aistra0528/Hail
         */
        Log.d("ShizukuHider", "updateComponentState: " + enabled);
        Method setComponentEnabledSetting;
        Object iPmInstance;
        
        Class <?> iPmClass = Class.forName("android.content.pm.IPackageManager");
        Class <?> iPmStub = Class.forName("android.content.pm.IPackageManager$Stub");
        Method asInterfaceMethod = iPmStub.getMethod("asInterface", IBinder.class);
        iPmInstance = asInterfaceMethod.invoke(null, new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));
        
        setComponentEnabledSetting = iPmClass.getMethod("setComponentEnabledSetting", ComponentName.class, int.class, int.class, int.class);
        
        setComponentEnabledSetting.invoke(iPmInstance,
                new ComponentName(packageName, componentName),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
                0);
        Log.i("ShizukuHider", "Updated component state: " + packageName);
    }
    
    public static Shell.Result execInternal(Shell.Command command, @Nullable InputStream inputPipe) {
        StringBuilder stdOutSb = new StringBuilder();
        StringBuilder stdErrSb = new StringBuilder();
        
        try {
            Shell.Command.Builder shCommand = new Shell.Command.Builder("sh", "-c", command.toString());
            
            //noinspection deprecation
            ShizukuRemoteProcess process = Shizuku.newProcess(shCommand.build().toStringArray(), null, null);
            
            Thread stdOutD = IOUtils.writeStreamToStringBuilder(stdOutSb, process.getInputStream());
            Thread stdErrD = IOUtils.writeStreamToStringBuilder(stdErrSb, process.getErrorStream());
            
            if (inputPipe != null) {
                try (OutputStream outputStream = process.getOutputStream(); InputStream inputStream = inputPipe) {
                    IOUtils.copyStream(inputStream, outputStream);
                } catch (Exception e) {
                    stdOutD.interrupt();
                    stdErrD.interrupt();
    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        process.destroyForcibly();
                    } else {
                        process.destroy();
                    }
                    
                    throw new RuntimeException(e);
                }
            }
            
            process.waitFor();
            stdOutD.join();
            stdErrD.join();
    
            return new Shell.Result(command, process.exitValue(), stdOutSb.toString().trim(), stdErrSb.toString().trim());
        } catch (Exception e) {
            Log.w(TAG, "Unable execute command: ");
            Log.w(TAG, e);
            return new Shell.Result(command, -1, stdOutSb.toString().trim(), stdErrSb
                    + "\n\n<!>ShizukuShell Java exception: " + Utils.throwableToString(e));
        }
    }
    
    public static void copyRishFiles(Context context) {
        AssetManager assetManager = context.getAssets();
        String[] files = {"rish/rish", "rish/rish_shizuku.dex"};
        
        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
    
                File outFile = new File(ShellPreferences.INSTANCE.getHomePath(), filename.substring(filename.lastIndexOf('/') + 1));
    
                if (outFile.exists()) {
                    Log.e("Shizuku", "File already exists: " + outFile.getAbsolutePath());
                } else {
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                }
            } catch (IOException e) {
                Log.e("Shizuku", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }
    
    public static String getRishCommand() {
        String echo = "echo Starting rish...";
        return echo + " && clear && cd $HOME && clear && sh rish && clear";
    }
    
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
