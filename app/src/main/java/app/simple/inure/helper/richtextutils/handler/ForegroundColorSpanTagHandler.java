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

import android.text.style.ForegroundColorSpan;

import org.xml.sax.Attributes;

import app.simple.inure.helper.richtextutils.SpanTagHandler;

public class ForegroundColorSpanTagHandler extends SpanTagHandler <ForegroundColorSpan> {
    public Class getSupportedCharacterStyle() {
        return (ForegroundColorSpan.class);
    }
    
    @Override
    public String findContextForTag(String name, Attributes a) {
        if ("font".equals(name)) {
            return (a.getValue("color"));
        }
        
        return (null);
    }
    
    @Override
    public ForegroundColorSpan buildSpanForTag(String name, Attributes a, String context) {
        return (new ForegroundColorSpan(0xFF000000 | Integer.parseInt(context.substring(1), 16)));
    }
    
    @Override
    public String getStartTagForSpan(ForegroundColorSpan span) {
        return ("<font color=\"#" + String.format("%06x", 0xFFFFFF & span.getForegroundColor()) + "\">");
    }
    
    @Override
    public String getEndTagForSpan(ForegroundColorSpan span) {
        return ("</font>");
    }
}
