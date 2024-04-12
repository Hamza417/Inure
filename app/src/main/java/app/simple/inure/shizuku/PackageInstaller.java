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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import app.simple.inure.adapters.apis.IIntentSenderAdapter;
import app.simple.inure.util.IntentSenderUtils;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;

public class PackageInstaller {
    private void install(List <Uri> uris, Context context) {
        android.content.pm.PackageInstaller packageInstaller;
        android.content.pm.PackageInstaller.Session session = null;
        ContentResolver cr = context.getContentResolver();
        StringBuilder res = new StringBuilder();
        String installerPackageName;
        String installerAttributionTag = null;
        int userId;
        boolean isRoot;
        
        try {
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
            res.append("createSession: ");
            
            android.content.pm.PackageInstaller.SessionParams params = new android.content.pm.PackageInstaller.SessionParams(android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int installFlags = PackageInstallerUtils.getInstallFlags(params);
            installFlags |= 0x00000004/*PackageManager.INSTALL_ALLOW_TEST*/ | 0x00000002/*PackageManager.INSTALL_REPLACE_EXISTING*/;
            PackageInstallerUtils.setInstallFlags(params, installFlags);
            
            sessionId = packageInstaller.createSession(params);
            res.append(sessionId).append('\n');
            
            res.append('\n').append("write: ");
            
            IPackageInstallerSession _session = IPackageInstallerSession.Stub.asInterface(new ShizukuBinderWrapper(_packageInstaller.openSession(sessionId).asBinder()));
            session = PackageInstallerUtils.createSession(_session);
            
            int i = 0;
            for (Uri uri : uris) {
                String name = i + ".apk";
                
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
                    }
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
                i++;
                
                Thread.sleep(1000);
            }
            
            res.append('\n').append("commit: ");
            
            Intent[] results = new Intent[]{null};
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
            res.append('\n').append("status: ").append(status).append(" (").append(message).append(")");
            
        } catch (Throwable tr) {
            tr.printStackTrace();
            res.append(tr);
        } finally {
            if (session != null) {
                try {
                    session.close();
                    
                } catch (Throwable tr) {
                    res.append(tr);
                }
            }
        }
    }
}
