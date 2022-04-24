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

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.WeakHashMap;

/**
 * Basket of rules (SpanTagHandler instances) for converting
 * CharacterStyle subclasses (spans) into XHTML tags. By default,
 * this just delegates to a static roster of SpanTagHandler
 * instances populated on SpanTagHandler. However, if you want
 * to override any of the default behaviors, register the SpanTagHandler
 * here, and these will take precedence.
 */
public class SpanTagRoster {
    final private ArrayList <SpanTagHandler> handlers =
            new ArrayList <SpanTagHandler>();
    final private WeakHashMap <Class, SpanTagHandler> handlersByCharacterStyle =
            new WeakHashMap <Class, SpanTagHandler>();
    
    /**
     * Defines a handler to use for converting a particular
     * CharacterStyle subclass to a particular XHTML tag structure
     *
     * @param handler the SpanTagHandler to register
     */
    public void registerSpanTagHandler(SpanTagHandler handler) {
        handlers.add(handler);
        handlersByCharacterStyle.put(handler.getSupportedCharacterStyle(), handler);
    }
    
    Object buildSpanForTag(String name, Attributes a) {
        for (SpanTagHandler h : handlers) {
            String context = h.findContextForTag(name, a);
            
            if (context != null) {
                return (h.buildSpanForTag(name, a, context));
            }
        }
        
        return (SpanTagHandler.buildSpanForTag(name, a));
    }
    
    <T> SpanTagHandler getSpanTagHandler(Class <T> cls) {
        SpanTagHandler result = handlersByCharacterStyle.get(cls);
        
        if (result == null) {
            result = SpanTagHandler.getGlobalSpanTagHandler(cls);
        }
        
        return (result);
    }
}
