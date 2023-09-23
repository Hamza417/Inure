package app.simple.inure.apk.parsers;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;

import app.simple.inure.R;
import app.simple.inure.database.instances.FOSSDatabase;
import app.simple.inure.models.FOSS;
import app.simple.inure.util.ProcessUtils;
import kotlin.Unit;

public class FOSSParser {
    
    private static HashMap <String, String> packageVersions;
    
    public static void init(Context context) {
        packageVersions = new HashMap <>();
        parsePackageVersions(context, R.xml.package_versions);
    }
    
    public static void parsePackageVersions(Context context, int xmlResourceId) {
        try {
            Resources resources = context.getResources();
            XmlResourceParser xmlParser = resources.getXml(xmlResourceId);
            
            String key = null;
            String value = null;
            
            int eventType = xmlParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xmlParser.getName().equals("string")) {
                        key = xmlParser.getAttributeValue(null, "name");
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    value = xmlParser.getText();
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xmlParser.getName().equals("string") && key != null && value != null) {
                        packageVersions.put(key, value);
                        key = null;
                        value = null;
                    }
                }
                eventType = xmlParser.next();
            }
    
            xmlParser.close();
    
            parseFromDatabase(context);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void parseFromDatabase(Context context) {
        ProcessUtils.INSTANCE.ensureNotOnMainThread(() -> {
            FOSSDatabase.Companion.getInstance(context)
                    .getFOSSDao().getAllFossMarkings().forEach(foss -> {
                        if (foss.isFOSS()) {
                            packageVersions.put(foss.getPackageName(), foss.getVersionCode());
                        } else {
                            packageVersions.remove(foss.getPackageName());
                        }
                    });
            return Unit.INSTANCE;
        });
    }
    
    public static String getPackageVersion(String packageName) {
        return packageVersions.get(packageName);
    }
    
    public static boolean isPackageFOSS(String packageName) {
        try {
            return packageVersions.containsKey(packageName);
        } catch (NullPointerException e) {
            return false;
        }
    }
    
    public static void addPackage(String packageName, String versionCode, Context context) {
        ProcessUtils.INSTANCE.ensureNotOnMainThread(() -> {
            packageVersions.put(packageName, versionCode);
            FOSSDatabase.Companion.getInstance(context)
                    .getFOSSDao().insertFOSS(
                            new FOSS(packageName, versionCode, true));
            return Unit.INSTANCE;
        });
    }
    
    public static void removePackage(String packageName, Context context) {
        ProcessUtils.INSTANCE.ensureNotOnMainThread(() -> {
            packageVersions.remove(packageName);
            FOSSDatabase.Companion.getInstance(context)
                    .getFOSSDao().insertFOSS(
                            new FOSS(packageName, "0", false));
            return Unit.INSTANCE;
        });
    }
}
