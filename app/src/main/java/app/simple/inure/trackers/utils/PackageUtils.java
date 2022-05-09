package app.simple.inure.trackers.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

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
        StringBuilder s = new StringBuilder(apkCert(packageInfo));
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
            s.append("\n");
            for (int i = 0; i < aPermissionsUse.length; i++) {
                s.append("\n\n").append(aPermissionsUse[i]);
            }
        }
        if (packageInfo.permissions != null) {
            s.append("\n\n#######################\n### Declared Permissions ###");
            try {
                Collections.sort(Arrays.asList(packageInfo.permissions), new Comparator <PermissionInfo>() {
                    public int compare(PermissionInfo o1, PermissionInfo o2) {
                        return o1.name.compareToIgnoreCase(o2.name);
                    }
                });
            } catch (NullPointerException e) {
            
            }
            for (int i = 0; i < packageInfo.permissions.length; i++) {
                s.append("\n\n\u25a0")
                        .append(packageInfo.permissions[i].name)
                        .append("\n")
                        .append(packageInfo.permissions[i].loadLabel(context.getPackageManager()))
                        .append("\n")
                        .append(packageInfo.permissions[i].loadDescription(context.getPackageManager()))
                        .append("\n").append(packageInfo.permissions[i].group);
            }
            
        }
        return s.toString();
    }
    
    public static String convertS(byte[] digest) {
        StringBuilder s = new StringBuilder();
        for (byte b : digest) {
            s.append(String.format("%02X", b).toLowerCase(Locale.getDefault()));
        }
        return s.toString();
    }
}
