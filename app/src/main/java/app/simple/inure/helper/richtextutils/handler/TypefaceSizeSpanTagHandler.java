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

import android.text.style.TypefaceSpan;

import org.xml.sax.Attributes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.simple.inure.helper.richtextutils.SpanTagHandler;

public class TypefaceSizeSpanTagHandler extends SpanTagHandler <TypefaceSpan> {
    static final Pattern FONTFAMILY_PATTERN = Pattern.compile("font-family:(serif|sans\\-serif|monospace);");
    private static final String FAMILY_SANS = "sans";
    private static final String GENERIC_SANS_SERIF = "sans-serif";
    
    @Override
    public Class getSupportedCharacterStyle() {
        return (TypefaceSpan.class);
    }
    
    @Override
    public String findContextForTag(String name, Attributes a) {
        if ("span".equals(name)) {
            String style = a.getValue("style");
            Matcher m = FONTFAMILY_PATTERN.matcher(style);
            
            if (m.find()) {
                return (m.group(1));
            }
        }
        
        return (null);
    }
    
    @Override
    public TypefaceSpan buildSpanForTag(String name, Attributes a, String context) {
        if (GENERIC_SANS_SERIF.equals(context)) {
            return (new TypefaceSpan(FAMILY_SANS));
        }
        
        return (new TypefaceSpan(context));
    }
    
    @Override
    public String getStartTagForSpan(TypefaceSpan span) {
        String family = span.getFamily();
        
        if (FAMILY_SANS.equals(family)) {
            family = GENERIC_SANS_SERIF;
        }
        
        return (String.format("<span style=\"font-family:%s;\">", family));
    }
    
    @Override
    public String getEndTagForSpan(TypefaceSpan span) {
        return ("</span>");
    }
}
