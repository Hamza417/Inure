/***
 Copyright (c) 2015 CommonsWare, LLC
 
 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package app.simple.inure.helper.richtextutils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Browser;
import android.text.style.ClickableSpan;
import android.view.View;

public abstract class AltURLSpan extends ClickableSpan {
    abstract public void onClick(View v);
    
    private final String url;
    
    AltURLSpan(String url) {
        this.url = url;
    }
    
    public String getURL() {
        return (url);
    }
    
    protected void launch(Context ctxt, Intent i) {
        i.putExtra(Browser.EXTRA_APPLICATION_ID, ctxt.getPackageName());
        ctxt.startActivity(i);
    }
    
    static public class Simple extends AltURLSpan {
        public Simple(String url) {
            super(url);
        }
        
        @Override
        public void onClick(View v) {
            launch(v.getContext(),
                    new Intent(Intent.ACTION_VIEW, Uri.parse(getURL())));
        }
    }
    
    static public class Chooser extends AltURLSpan {
        final String dialogTitle;
        
        public Chooser(String url, String dialogTitle) {
            super(url);
            this.dialogTitle = dialogTitle;
        }
        
        @Override
        public void onClick(View v) {
            launch(v.getContext(),
                    Intent.createChooser(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(getURL())),
                            dialogTitle));
        }
    }
    
    static public class DefaultOnly extends AltURLSpan {
        public DefaultOnly(String url) {
            super(url);
        }
        
        @Override
        public void onClick(View v) {
            PackageManager mgr = v.getContext().getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getURL()));
            ResolveInfo launchable =
                    mgr.resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY);
            ActivityInfo activity = launchable.activityInfo;
            ComponentName name =
                    new ComponentName(activity.applicationInfo.packageName,
                            activity.name);
            i.setComponent(name);
            launch(v.getContext(), i);
        }
    }
}
