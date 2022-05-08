/*
 * Copyright 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.simple.inure.trackers.utils;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import app.simple.inure.util.IOUtils;

public class UriUtils {
    
    public static InputStream getStreamFromUri(Context context, Uri uriFromIntent) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(uriFromIntent);
    }
    
    public static boolean isAttach(Uri uriFromIntent) {
        return (uriFromIntent != null) && (uriFromIntent.getScheme().contains("content"));
    }
    
    public static String pathUriCache(Context context, Uri uri, String nCache) {
        File f = new File(context.getCacheDir(), nCache);//sdk30 getExternalFilesDir(null)
        try {
            FileOutputStream fos = new FileOutputStream(f);
            IOUtils.copyLarge(context.getContentResolver().openInputStream(uri), fos);
            
            return f.getPath();
        } catch (IOException e) {
            return null;
        }
        
    }
    
    public static String stringUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 4];
            for (int length; (length = inputStream.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            // StandardCharsets.UTF_8.name() > JDK 7 https://stackoverflow.com/questions/309424/how-do-i-read-convert-an-inputstream-into-a-string-in-java
            return result.toString("UTF-8");
        } catch (Exception e) {
            return e.toString();
        }
    }
    
}
