package android.content.pm;

import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import java.util.List;

import androidx.annotation.RequiresApi;

public interface IPackageManager extends IInterface {
    
    IPackageInstaller getPackageInstaller()
            throws RemoteException;
    
    boolean isPackageAvailable(String packageName, int userId)
            throws RemoteException;
    
    ApplicationInfo getApplicationInfo(String packageName, int flags, int userId)
            throws RemoteException;
    
    @RequiresApi (33)
    ApplicationInfo getApplicationInfo(String packageName, long flags, int userId)
            throws RemoteException;
    
    PackageInfo getPackageInfo(String packageName, int flags, int userId)
            throws RemoteException;
    
    @RequiresApi (33)
    PackageInfo getPackageInfo(String packageName, long flags, int userId)
            throws RemoteException;
    
    int getPackageUid(String packageName, int userId)
            throws RemoteException;
    
    @RequiresApi (24)
    int getPackageUid(String packageName, int flags, int userId)
            throws RemoteException;
    
    @RequiresApi (33)
    int getPackageUid(String packageName, long flags, int userId)
            throws RemoteException;
    
    String[] getPackagesForUid(int uid)
            throws RemoteException;
    
    ParceledListSlice <PackageInfo> getInstalledPackages(int flags, int userId)
            throws RemoteException;
    
    @RequiresApi (33)
    ParceledListSlice <PackageInfo> getInstalledPackages(long flags, int userId)
            throws RemoteException;
    
    ParceledListSlice <ApplicationInfo> getInstalledApplications(int flags, int userId)
            throws RemoteException;
    
    @RequiresApi (33)
    ParceledListSlice <ApplicationInfo> getInstalledApplications(long flags, int userId)
            throws RemoteException;
    
    int getUidForSharedUser(String sharedUserName)
            throws RemoteException;
    
    void grantRuntimePermission(String packageName, String permissionName, int userId)
            throws RemoteException;
    
    void revokeRuntimePermission(String packageName, String permissionName, int userId)
            throws RemoteException;
    
    int getPermissionFlags(String permissionName, String packageName, int userId)
            throws RemoteException;
    
    void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, int userId)
            throws RemoteException;
    
    @RequiresApi (29)
    void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, boolean checkAdjustPolicyFlagPermission, int userId)
            throws RemoteException;
    
    int checkPermission(String permName, String pkgName, int userId)
            throws RemoteException;
    
    int checkUidPermission(String permName, int uid)
            throws RemoteException;
    
    boolean getApplicationHiddenSettingAsUser(String packageName, int userId)
            throws RemoteException;
    
    ProviderInfo resolveContentProvider(String name, int flags, int userId)
            throws RemoteException;
    
    @RequiresApi (33)
    ProviderInfo resolveContentProvider(String name, long flags, int userId)
            throws RemoteException;
    
    int installExistingPackageAsUser(String packageName, int userId, int installFlags, int installReason)
            throws RemoteException;
    
    @RequiresApi (29)
    int installExistingPackageAsUser(String packageName, int userId, int installFlags, int installReason, List <String> whiteListedPermissions)
            throws RemoteException;
    
    ParceledListSlice <ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId)
            throws RemoteException;
    
    @RequiresApi (33)
    ParceledListSlice <ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, long flags, int userId)
            throws RemoteException;
    
    boolean performDexOptMode(String packageName, boolean checkProfiles, String targetCompilerFilter, boolean force, boolean bootComplete, String splitName)
            throws RemoteException;
    
    int checkSignatures(String pkg1, String pkg2)
            throws RemoteException;
    
    int checkUidSignatures(int uid1, int uid2)
            throws RemoteException;
    
    PermissionGroupInfo getPermissionGroupInfo(String groupName, int flags)
            throws RemoteException;
    
    PermissionInfo getPermissionInfo(String permissionName, int flags)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.O)
    PermissionInfo getPermissionInfo(String permissionName, String packageName, int flags)
            throws RemoteException;
    
    abstract class Stub extends Binder implements IPackageManager {
        
        public static IPackageManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
