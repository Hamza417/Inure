/*
 * Copyright (C) 2013 Simon Marquis (http://www.simon-marquis.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package app.simple.inure.models;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;

import app.simple.inure.apk.sharedPrefs.Utils;

public class AppEntry {
    
    /**
     *
     */
    private final ApplicationInfo mInfo;
    /**
     * File of the application
     */
    private final File mApkFile;
    /**
     * Label of the application
     */
    private String mLabel;
    /**
     * Value used to sort the list of applications
     */
    private String mSortingValue;
    /**
     * Detect if app is starred by user
     */
    private boolean isFavorite;
    /**
     * Char value used by indexed ListView
     */
    private char headerChar;
    /**
     * Uri of the app icon
     */
    private Uri mIconUri;
    
    public AppEntry(ApplicationInfo info, Context context) {
        mInfo = info;
        isFavorite = Utils.isFavorite(mInfo.packageName, context);
        mApkFile = new File(info.sourceDir);
        loadLabels(context);
        buildIconUri(mInfo);
    }
    
    public ApplicationInfo getApplicationInfo() {
        return mInfo;
    }
    
    public String getLabel() {
        return mLabel;
    }
    
    public String getSortingValue() {
        return mSortingValue;
    }
    
    private void buildIconUri(ApplicationInfo info) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(ContentResolver.SCHEME_ANDROID_RESOURCE);
        builder.authority(info.packageName);
        builder.appendPath(Integer.toString(info.icon));
        mIconUri = builder.build();
    }
    
    public Uri getIconUri() {
        return mIconUri;
    }
    
    @Override
    public String toString() {
        return mLabel;
    }
    
    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
        // IMPORTANT! also update the char used for sorting
        mSortingValue = (isFavorite ? " " : "") + mLabel;
        headerChar = formatChar(mLabel);
    }
    
    /**
     * Generate the labels
     *
     * @param ctx .
     */
    private void loadLabels(Context ctx) {
        if (mLabel == null) {
            if (!mApkFile.exists()) {
                mLabel = mInfo.packageName;
            } else {
                PackageManager pm = ctx.getPackageManager();
                CharSequence label = null;
                if (pm != null) {
                    label = mInfo.loadLabel(pm);
                }
                mLabel = label != null ? label.toString() : mInfo.packageName;
            }
            
            // replace false spaces O_o
            mLabel = mLabel.replaceAll("\\s", " ");
        }
    
        if (mSortingValue == null) {
            mSortingValue = (isFavorite ? " " : "") + mLabel;
        }
        
        headerChar = formatChar(mLabel);
    }
    
    /**
     * Generate a char from a string to index the entry
     *
     * @param s .
     * @return .
     */
    private char formatChar(String s) {
        if (isFavorite) {
            return '☆';
        }
        
        if (TextUtils.isEmpty(s)) {
            return '#';
        }
        
        char c = Character.toUpperCase(s.charAt(0));
        
        // Number
        if (c >= '0' && c <= '9') {
            return '#';
        }
        
        // Letter
        if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
            return c;
        }
        
        // Accented letter
        switch (c) {
            case 'À':
            case 'Á':
            case 'Â':
            case 'Ã':
            case 'Ä':
                return 'A';
            case 'É':
            case 'È':
            case 'Ê':
            case 'Ë':
                return 'E';
        }
        
        // Everything else
        return '#';
    }
    
    public char getHeaderChar() {
        return headerChar;
    }
}