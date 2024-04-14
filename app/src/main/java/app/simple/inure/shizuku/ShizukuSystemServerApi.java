package app.simple.inure.shizuku;

import android.content.Context;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.IUserManager;
import android.os.RemoteException;

import java.util.List;

import androidx.annotation.RequiresApi;
import app.simple.inure.generics.Singleton;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class ShizukuSystemServerApi {

    private static final Singleton <IPackageManager> PACKAGE_MANAGER = new Singleton <>() {
        @Override
        protected IPackageManager create() {
            return IPackageManager.Stub.asInterface(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));
        }
    };

    private static final Singleton<IUserManager> USER_MANAGER = new Singleton <>() {
        @Override
        protected IUserManager create() {
            return IUserManager.Stub.asInterface(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.USER_SERVICE)));
        }
    };

    public static IPackageInstaller PackageManager_getPackageInstaller() throws RemoteException {
        IPackageInstaller packageInstaller = PACKAGE_MANAGER.get().getPackageInstaller();
        return IPackageInstaller.Stub.asInterface(new ShizukuBinderWrapper(packageInstaller.asBinder()));
    }

    @RequiresApi (api = Build.VERSION_CODES.R)
    public static List<UserInfo> UserManager_getUsers(boolean excludePartial, boolean excludeDying, boolean excludePreCreated) throws RemoteException {
        if (Build.VERSION.SDK_INT >= 30) {
            return USER_MANAGER.get().getUsers(excludePartial, excludeDying, excludePreCreated);
        } else {
            try {
                return USER_MANAGER.get().getUsers(excludeDying);
            } catch (NoSuchFieldError e) {
                return USER_MANAGER.get().getUsers(excludePartial, excludeDying, excludePreCreated);
            }
        }
    }
}
