package app.simple.inure.apk.parsers;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;

import app.simple.inure.R;

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
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
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
}
