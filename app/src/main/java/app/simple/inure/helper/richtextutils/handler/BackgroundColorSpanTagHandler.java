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

package app.simple.inure.helper.richtextutils.handler;

import android.text.style.BackgroundColorSpan;

import org.xml.sax.Attributes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.simple.inure.helper.richtextutils.SpanTagHandler;

public class BackgroundColorSpanTagHandler extends SpanTagHandler <BackgroundColorSpan> {
    static final Pattern BGCOLOR_PATTERN = Pattern.compile("background-color:#([0-9a-fA-F]{6});");
    
    @Override
    public Class getSupportedCharacterStyle() {
        return (BackgroundColorSpan.class);
    }
    
    @Override
    public String findContextForTag(String name, Attributes a) {
        if ("span".equals(name)) {
            String style = a.getValue("style");
            Matcher m = BGCOLOR_PATTERN.matcher(style);
            
            if (m.find()) {
                return (m.group(1));
            }
        }
        
        return (null);
    }
    
    @Override
    public BackgroundColorSpan buildSpanForTag(String name, Attributes a, String context) {
        return (new BackgroundColorSpan(0xFF000000 | Integer.parseInt(context, 16)));
    }
    
    @Override
    public String getStartTagForSpan(BackgroundColorSpan span) {
        return ("<span style=\"background-color:#" + String.format("%06x", 0xFFFFFF & span.getBackgroundColor()) + ";\">");
    }
    
    @Override
    public String getEndTagForSpan(BackgroundColorSpan span) {
        return ("</span>");
    }
}
