package app.simple.inure.apk.installer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import app.simple.inure.services.InstallerSessionService;

public class InstallerUtils {
    
    private static PackageInstaller.SessionParams sessionParams;
    
    public static PackageInstaller.SessionParams makeInstallParams(long totalSize) {
        sessionParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            sessionParams.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
        }
    
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sessionParams.setInstallReason(PackageManager.INSTALL_REASON_USER);
        }
        sessionParams.setSize(totalSize);
        return sessionParams;
    }
    
    public static int createSession(PackageInstaller.SessionParams params, Context context) {
        try {
            return context.getPackageManager().getPackageInstaller().createSession(params);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public static void installWriteSessions(int sessionId, File file, Context context) {
        PackageInstaller.Session session = null;
        InputStream in = null;
        OutputStream out = null;
        
        try {
            session = context.getPackageManager().getPackageInstaller().openSession(sessionId);
            if (file.getPath() != null) {
                in = new FileInputStream(file.getPath());
            }
            out = session.openWrite(file.getName(), 0, file.length());
            byte[] buffer = new byte[65536];
            int c;
            assert in != null;
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            session.fsync(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert out != null;
                out.close();
                assert in != null;
                in.close();
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void commitSession(int sessionId, Context context) {
        PackageInstaller.Session session = null;
        Intent callbackIntent = new Intent(context, InstallerSessionService.class);
        
        try {
            try {
                session = context.getPackageManager().getPackageInstaller().openSession(sessionId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            PendingIntent pendingIntent = PendingIntent.getService(context, 123, callbackIntent,
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0);
            
            session.commit(pendingIntent.getIntentSender());
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert session != null;
            session.close();
        }
    }
    
    public static PackageInstaller.SessionParams getInstallerSessionParams() {
        if (sessionParams != null) {
            return sessionParams;
        } else {
            throw new NullPointerException("Create a package installer session first");
        }
    }
}
