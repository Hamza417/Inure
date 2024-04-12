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
import app.simple.inure.util.IntentSenderUtils;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;

public class PackageInstaller {
    
    /** @noinspection FieldCanBeLocal*/
    private final String TAG = "PackageInstaller";
    
    public void install(List <Uri> uris, Context context)
            throws InterruptedException,
            InvocationTargetException,
            NoSuchMethodException,
            IllegalAccessException,
            InstantiationException,
            RemoteException,
            NoSuchFieldException,
            IOException {
        
        android.content.pm.PackageInstaller packageInstaller;
        android.content.pm.PackageInstaller.Session session = null;
        ContentResolver cr = context.getContentResolver();
        
        String installerPackageName;
        String installerAttributionTag = null;
        
        int userId;
        boolean isRoot;
        
        IPackageInstaller _packageInstaller = ShizukuSystemServerApi.PackageManager_getPackageInstaller();
        isRoot = Shizuku.getUid() == 0;
        
        // the reason for use "com.android.shell" as installer package under adb is that getMySessions will check installer package's owner
        installerPackageName = isRoot ? context.getPackageName() : "com.android.shell";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installerAttributionTag = context.getAttributionTag();
        }
        userId = isRoot ? Process.myUserHandle().hashCode() : 0;
        packageInstaller = PackageInstallerUtils.createPackageInstaller(_packageInstaller, installerPackageName, installerAttributionTag, userId);
        int sessionId;
        Log.d(TAG, "install: createSession " + installerPackageName + " " + installerAttributionTag + " " + userId);
        
        android.content.pm.PackageInstaller.SessionParams params = new android.content.pm.PackageInstaller.SessionParams(android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        int installFlags = PackageInstallerUtils.getInstallFlags(params);
        installFlags |= 0x00000004/*PackageManager.INSTALL_ALLOW_TEST*/ | 0x00000002/*PackageManager.INSTALL_REPLACE_EXISTING*/;
        PackageInstallerUtils.setInstallFlags(params, installFlags);
        
        sessionId = packageInstaller.createSession(params);
        Log.d(TAG, "install: sessionId " + sessionId);
        
        Log.d(TAG, "install: beginning write");
        IPackageInstallerSession _session = IPackageInstallerSession.Stub.asInterface(new ShizukuBinderWrapper(_packageInstaller.openSession(sessionId).asBinder()));
        session = PackageInstallerUtils.createSession(_session);
        
        int i = 0;
        for (Uri uri : uris) {
            String name = i + ".apk";
            Log.d(TAG, "install: write " + name);
            
            InputStream is = cr.openInputStream(uri);
            OutputStream os = session.openWrite(name, 0, -1);
            
            byte[] buf = new byte[8192];
            int len;
            try {
                while ((len = is.read(buf)) > 0) {
                    os.write(buf, 0, len);
                    os.flush();
                    session.fsync(os);
                }
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "install: ", e);
                }
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            i++;
            Log.d(TAG, "install: write " + name + " done");
        }
        
        Log.d(TAG, "install: commit");
        
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
        
        Log.d(TAG, "install: session closed");
    }
}
