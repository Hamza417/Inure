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

import android.text.style.CharacterStyle;

import org.xml.sax.Attributes;

import app.simple.inure.helper.richtextutils.SpanTagHandler;

abstract public class ClassSpanTagHandler <T extends CharacterStyle> extends SpanTagHandler <T> {
    private final String cssClass;
    
    public ClassSpanTagHandler(String cssClass) {
        this.cssClass = cssClass;
    }
    
    @Override
    public String getStartTagForSpan(T span) {
        return (String.format("<span class=\"%s\">", cssClass));
    }
    
    @Override
    public String getEndTagForSpan(T span) {
        return ("</span>");
    }
    
    @Override
    public String findContextForTag(String name, Attributes a) {
        if ("span".equals(name)) {
            String requestedCssClass = a.getValue("class");
            
            if (cssClass.equals(requestedCssClass)) {
                return (cssClass);
            }
        }
        
        return (null);
    }
}
