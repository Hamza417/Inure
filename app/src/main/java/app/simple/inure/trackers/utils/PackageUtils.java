package app.simple.inure.trackers.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import androidx.appcompat.app.AlertDialog;
import app.simple.inure.R;
import app.simple.inure.apk.utils.PermissionUtils;

public class PackageUtils {
    
    public static String apkIsolatedZygote(PackageInfo p, String intro) {
        String sResult = "";
        ServiceInfo[] si = p.services;
        if (si != null) {
            for (ServiceInfo s : si) {
                if ((s.flags & ServiceInfo.FLAG_USE_APP_ZYGOTE) != 0) {
                    sResult = s.name + ":" + sResult;
                    sResult += "\u26BF";
                }
            }
        }
        
        return sResult.length() == 0 ? "" : intro + sResult;
    }
    
    public static String apkCert(PackageInfo packageInfo) {
        Signature[] z = packageInfo.signatures;
        String s = "";
        X509Certificate c;
        try {
            for (Signature sg : z) {
                c = (X509Certificate) CertificateFactory.getInstance("X.509")
                        .generateCertificate(new ByteArrayInputStream(sg.toByteArray()));
                s = "\n\n" + c.getIssuerX500Principal().getName() + "\n\n" + c.getSigAlgName();
                try {
                    s += "\n\nCERTIFICATE fingerprints: \nmd5: " + convertS(MessageDigest.getInstance("md5").digest(sg.toByteArray()));
                    s += "\nsha1: " + convertS(MessageDigest.getInstance("sha1").digest(sg.toByteArray()));
                    s += "\nsha256: " + convertS(MessageDigest.getInstance("sha256").digest(sg.toByteArray()));
                    
                } catch (NoSuchAlgorithmException e) {
                    return e.getMessage();
                }
            }
            
        } catch (CertificateException e) {
            return e.getMessage();
        }
        return s;
    }
    
    public static String apkPro(PackageInfo packageInfo, Context context) {
        String[] aPermissionsUse;
        String s = apkCert(packageInfo);
        String tmp = "";
        PermissionInfo pI;
        
        if (packageInfo.requestedPermissions != null) {
            aPermissionsUse = new String[packageInfo.requestedPermissions.length];
            for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
                if (packageInfo.requestedPermissions[i].startsWith("android.permission")) {
                    aPermissionsUse[i] = packageInfo.requestedPermissions[i].substring(18) + " ";
                } else {
                    aPermissionsUse[i] = packageInfo.requestedPermissions[i] + " ";
                }
                try {
                    pI = context.getPackageManager().getPermissionInfo(packageInfo.requestedPermissions[i], PackageManager.GET_META_DATA);
                    tmp = PermissionUtils.INSTANCE.protectionToString(pI.protectionLevel, pI.flags, context);
                    if (tmp.contains("dangerous")) {
                        aPermissionsUse[i] = "*\u2638" + aPermissionsUse[i];
                    }
                    aPermissionsUse[i] += tmp + "\n-->" + pI.group;
                    
                } catch (PackageManager.NameNotFoundException e) {
                
                }
                if ((packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    aPermissionsUse[i] += " ^\u2714";
                }
                
            }
            try {
                Arrays.sort(aPermissionsUse);
            } catch (NullPointerException e) {
            }
            s += "\n";
            for (int i = 0; i < aPermissionsUse.length; i++) {
                s += "\n\n" + aPermissionsUse[i];
            }
        }
        if (packageInfo.permissions != null) {
            s += "\n\n#######################\n### Declared Permissions ###";
            try {
                Collections.sort(Arrays.asList(packageInfo.permissions), new Comparator <PermissionInfo>() {
                    public int compare(PermissionInfo o1, PermissionInfo o2) {
                        return o1.name.compareToIgnoreCase(o2.name);
                    }
                });
            } catch (NullPointerException e) {
            
            }
            for (int i = 0; i < packageInfo.permissions.length; i++) {
                s += "\n\n\u25a0" + packageInfo.permissions[i].name
                        + "\n" + packageInfo.permissions[i].loadLabel(context.getPackageManager())
                        + "\n" + packageInfo.permissions[i].loadDescription(context.getPackageManager())
                        + "\n" + packageInfo.permissions[i].group;
            }
            
        }
        return s;
    }
    
    public static String convertS(byte[] digest) {
        String s = "";
        for (byte b : digest) {
            s += String.format("%02X", b).toLowerCase();
        }
        return s;
    }
    
    public static File appPublicSourceDir(Context context, String packageName) {
        try {
            return new File(context.getPackageManager().getApplicationInfo(packageName, 0).publicSourceDir);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
    
    @SuppressLint ("SetTextI18n")
    public static void manifestPackage(Resources r, String code, Context ctx) {
        StringBuilder s = new StringBuilder();
        String[] Sign;
        int Signz = 0;
        int Totalz = 0;
        String[] Names;
        
        Names = r.getStringArray(R.array.tname);
        Sign = r.getStringArray(R.array.trackers);
        for (Signz = 0; Signz < Sign.length; Signz++) {
            if (code.contains(Sign[Signz])) {
                s.append("_").append(Names[Signz]).append(": ").append(Sign[Signz]).append("\n\n");
                Totalz++;
            }
        }
        //Log.e("zzz",s);
        TextView showText = new TextView(ctx);
        showText.setText("\u2211 = " + Totalz + " exoTracker(s)\n\n" + s);
        showText.setTextIsSelectable(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setView(showText)
                .setTitle(r.getString(R.string.exodus) + " " + r.getString(R.string.scan))
                .setIcon(R.mipmap.ic_launcher_round)
                .setCancelable(true)
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }
}
