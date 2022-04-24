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

import android.text.style.RelativeSizeSpan;

import org.xml.sax.Attributes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.simple.inure.helper.richtextutils.SpanTagHandler;

public class RelativeSizeSpanTagHandler extends SpanTagHandler <RelativeSizeSpan> {
    static final Pattern RELSIZE_PATTERN = Pattern.compile("font-size:([0-9]+)%;");
    
    @Override
    public Class getSupportedCharacterStyle() {
        return (RelativeSizeSpan.class);
    }
    
    @Override
    public String findContextForTag(String name, Attributes a) {
        if ("span".equals(name)) {
            String style = a.getValue("style");
            Matcher m = RELSIZE_PATTERN.matcher(style);
            
            if (m.find()) {
                return (m.group(1));
            }
        }
        
        return (null);
    }
    
    @Override
    public RelativeSizeSpan buildSpanForTag(String name, Attributes a, String context) {
        return (new RelativeSizeSpan(Float.parseFloat(context) / 100.0f));
    }
    
    @Override
    public String getStartTagForSpan(RelativeSizeSpan span) {
        return (String.format("<span style=\"font-size:%d%%;\">", (int) (100.0f * span.getSizeChange())));
    }
    
    @Override
    public String getEndTagForSpan(RelativeSizeSpan span) {
        return ("</span>");
    }
}
