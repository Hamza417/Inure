package app.simple.inure.shizuku;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.system.Os;
import android.util.Log;

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
        Call android.content.pm.IPackageManager.setApplicationEnabledSetting with reflection.
        Through Shizuku wrapper.
        Reference:
            - https://www.xda-developers.com/implementing-shizuku/
            - https://github.dev/aistra0528/Hail
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
}
