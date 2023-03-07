package app.simple.inure.shizuku;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.system.Os;
import android.util.Log;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import app.simple.inure.BuildConfig;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class ShizukuUtils {
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
    public static void uninstallApp(Set <String> pkgNames)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        /*
         * Call android.content.pm.IPackageManager.deletePackage with reflection.
         * Through Shizuku wrapper.
         * References:
         *             - https://www.xda-developers.com/implementing-shizuku/
         *             - https://github.dev/aistra0528/Hail
         */
        Log.d("ShizukuHider", "uninstallApp");
        Method deletePackage;
        Object iPmInstance;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("Landroid/content/pm/IPackageManager;"); // one specific class
        }
        
        Class <?> iPmClass = Class.forName("android.content.pm.IPackageManager");
        Class <?> iPmStub = Class.forName("android.content.pm.IPackageManager$Stub");
        Method asInterfaceMethod = iPmStub.getMethod("asInterface", IBinder.class);
        iPmInstance = asInterfaceMethod.invoke(null, new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            deletePackage = HiddenApiBypass.getDeclaredMethod(iPmClass, "deletePackage", String.class, IPackageDeleteObserver2.class, int.class, int.class);
        } else {
            deletePackage = iPmClass.getMethod("deletePackage", String.class, IPackageDeleteObserver2.class, int.class, int.class);
        }
        
        for (String packageName : pkgNames) {
            deletePackage.invoke(iPmInstance, packageName, new IPackageDeleteObserver2.Stub() {
                @Override
                public void onPackageDeleted(String s, int i, String s1) {
                    Log.i("ShizukuHider", "Uninstalled app: " + s);
                }
                
                @Override
                public void onUserActionRequired(Intent intent) {
                    Log.i("ShizukuHider", "Uninstalled app: " + intent);
                }
            }, 0, Os.getuid() / 100000);
            Log.i("ShizukuHider", "Uninstalled app: " + packageName);
        }
    }
}
