package app.simple.inure.shizuku;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageInstallerSession;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import app.simple.inure.adapters.apis.IIntentSenderAdapter;
import app.simple.inure.models.ShizukuInstall;
import app.simple.inure.preferences.InstallerPreferences;
import app.simple.inure.util.IntentSenderUtils;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;

/**
 * @noinspection FieldCanBeLocal
 */
public class PackageInstaller {
    
    private final String TAG = "PackageInstaller";
    private final int INSTALL_ALL_WHITELIST_RESTRICTED_PERMISSIONS = 0x00400000;
    
    public ShizukuInstall install(List <Uri> uris, Context context) throws Exception {
        android.content.pm.PackageInstaller packageInstaller;
        android.content.pm.PackageInstaller.Session session;
        ContentResolver contentResolver = context.getContentResolver();
        
        String installerPackageName;
        String installerAttributionTag = null;
        
        int userId;
        boolean isRootUser;
        
        IPackageInstaller packageInstallerService = ShizukuSystemServerApi.PackageManager_getPackageInstaller();
        isRootUser = Shizuku.getUid() == 0;
        
        installerPackageName = getInstallerPackageName(context, isRootUser);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installerAttributionTag = context.getAttributionTag();
        }
        userId = getUserId(isRootUser);
        packageInstaller = createPackageInstaller(packageInstallerService, installerPackageName, installerAttributionTag, userId);
        
        int sessionId = createSession(packageInstaller);
        session = openSession(packageInstallerService, sessionId);
        
        writeApkFilesToSession(uris, contentResolver, session);
        
        return commitSession(session);
    }
    
    private String getInstallerPackageName(Context context, boolean isRootUser) {
        return isRootUser ? InstallerPreferences.INSTANCE.getInstallerPackageName(context) : "com.android.shell";
    }
    
    private int getUserId(boolean isRootUser) {
        return isRootUser ? Process.myUserHandle().hashCode() : 0;
    }
    
    private android.content.pm.PackageInstaller createPackageInstaller(
            IPackageInstaller packageInstallerService, String installerPackageName, String installerAttributionTag, int userId)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return PackageInstallerUtils.createPackageInstaller(packageInstallerService, installerPackageName, installerAttributionTag, userId);
    }
    
    private int createSession(android.content.pm.PackageInstaller packageInstaller)
            throws IOException, NoSuchFieldException, IllegalAccessException {
        android.content.pm.PackageInstaller.SessionParams sessionParams =
                new android.content.pm.PackageInstaller.SessionParams(android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        int installFlags = getInstallFlags();
        PackageInstallerUtils.setInstallFlags(sessionParams, installFlags);
        
        return packageInstaller.createSession(sessionParams);
    }
    
    private android.content.pm.PackageInstaller.Session openSession(IPackageInstaller packageInstallerService, int sessionId)
            throws RemoteException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        IPackageInstallerSession installerSession = IPackageInstallerSession.Stub.asInterface(new ShizukuBinderWrapper(packageInstallerService.openSession(sessionId).asBinder()));
        return PackageInstallerUtils.createSession(installerSession);
    }
    
    private void writeApkFilesToSession(List <Uri> uris, ContentResolver contentResolver, android.content.pm.PackageInstaller.Session session) throws IOException {
        int i = 0;
        for (Uri uri : uris) {
            String name = i + ".apk";
            
            try (InputStream inputStream = contentResolver.openInputStream(uri);
                 OutputStream outputStream = session.openWrite(name, 0, -1)) {
                
                byte[] buffer = new byte[8192];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                    outputStream.flush();
                    session.fsync(outputStream);
                }
            }
            
            i++;
        }
    }
    
    private ShizukuInstall commitSession(android.content.pm.PackageInstaller.Session session)
            throws InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Intent[] results = new Intent[] {null};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        IntentSender intentSender = IntentSenderUtils.newInstance(new IIntentSenderAdapter() {
            @Override
            public void send(Intent intent) {
                results[0] = intent;
                countDownLatch.countDown();
            }
        });
        
        session.commit(intentSender);
        
        countDownLatch.await();
        Intent result = results[0];
        int status = result.getIntExtra(android.content.pm.PackageInstaller.EXTRA_STATUS, android.content.pm.PackageInstaller.STATUS_FAILURE);
        String message = result.getStringExtra(android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE);
        Log.d(TAG, "install: commit done with status " + status + " (" + message + ")");
        
        session.close();
        
        return new ShizukuInstall(status, message);
    }
    
    private int getInstallFlags() {
        int flags = 0;
        
        if (InstallerPreferences.INSTANCE.isGrantRuntimePermissions()) {
            flags |= 0x00000100; // PackageManager.INSTALL_GRANT_RUNTIME_PERMISSIONS
        }
        if (InstallerPreferences.INSTANCE.isTestPackages()) {
            flags |= 0x00000004; // PackageManager.INSTALL_ALLOW_TEST
        }
        if (InstallerPreferences.INSTANCE.isReplaceExisting()) {
            flags |= 0x00000002; // PackageManager.INSTALL_REPLACE_EXISTING
        }
        if (InstallerPreferences.INSTANCE.isBypassLowTargetSdk()) {
            flags |= 0x01000000; // PackageManager.INSTALL_BYPASS_LOW_TARGET_SDK_BLOCK
        }
        
        // flags |= 0x00000008; // PackageManager.INSTALL_FORCE_VOLUME_UUID
        
        if (InstallerPreferences.INSTANCE.isVersionCodeDowngrade()) {
            flags |= 0x00100000; // PackageManager.INSTALL_ALLOW_DOWNGRADE
            flags |= 0x00000080; // PackageManager.INSTALL_REQUEST_DOWNGRADE
        }
        
        if (InstallerPreferences.INSTANCE.isDontKill()) {
            flags |= 0x00001000; // PackageManager.INSTALL_DONT_KILL_APP
        }
        
        return flags;
    }
}
