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

import android.graphics.Typeface;
import android.text.style.StyleSpan;

import org.xml.sax.Attributes;

import app.simple.inure.helper.richtextutils.SpanTagHandler;

public class StyleSpanTagHandler extends SpanTagHandler <StyleSpan> {
    private static final String[] TAGS = {"b", "i"};
    
    public String[] getSupportedTags() {
        return (TAGS);
    }
    
    public Class getSupportedCharacterStyle() {
        return (StyleSpan.class);
    }
    
    public String findContextForTag(String name, Attributes a) {
        for (String tag : getSupportedTags()) {
            if (tag.equals(name)) {
                return (name);
            }
        }
        
        return (null);
    }
    
    @Override
    public StyleSpan buildSpanForTag(String name, Attributes a, String context) {
        if ("b".equals(name)) {
            return (new StyleSpan(Typeface.BOLD));
        }
        
        return (new StyleSpan(Typeface.ITALIC));
    }
    
    @Override
    public String getStartTagForSpan(StyleSpan span) {
        switch (span.getStyle()) {
            case Typeface.BOLD:
                return ("<b>");
            
            case Typeface.ITALIC:
                return ("<i>");
        }
        
        throw new IllegalArgumentException("Unrecognized span");
    }
    
    @Override
    public String getEndTagForSpan(StyleSpan span) {
        if (span.getStyle() == Typeface.BOLD) {
            return ("</b>");
        } else if (span.getStyle() == Typeface.ITALIC) {
            return ("</i>");
        }
        
        throw new IllegalArgumentException("Unrecognized span");
    }
}
