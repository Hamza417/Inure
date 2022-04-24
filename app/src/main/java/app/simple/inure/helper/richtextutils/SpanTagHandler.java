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

import app.simple.inure.helper.richtextutils.handler.AbsoluteSizeSpanTagHandler;
import app.simple.inure.helper.richtextutils.handler.BackgroundColorSpanTagHandler;
import app.simple.inure.helper.richtextutils.handler.ForegroundColorSpanTagHandler;
import app.simple.inure.helper.richtextutils.handler.RelativeSizeSpanTagHandler;
import app.simple.inure.helper.richtextutils.handler.StrikethroughSpanTagHandler;
import app.simple.inure.helper.richtextutils.handler.StyleSpanTagHandler;
import app.simple.inure.helper.richtextutils.handler.SubscriptSpanTagHandler;
import app.simple.inure.helper.richtextutils.handler.SuperscriptSpanTagHandler;
import app.simple.inure.helper.richtextutils.handler.TypefaceSizeSpanTagHandler;
import app.simple.inure.helper.richtextutils.handler.URLSpanTagHandler;
import app.simple.inure.helper.richtextutils.handler.UnderlineSpanTagHandler;

/**
 * Class responsible for converting one CharacterStyle subclass
 * into XHTML tags, and vice versa. A stock set of SpanTagHandlers
 * is set up for you. If you have additional spans/tags that you want
 * to support, you can use the static registerGlobalSpanTagHandler()
 * method to add them. Or, you can register them with the
 * SpanTagRoster to use for a specific set of conversion
 * operations.
 *
 * @param <T> the specific CharacterStyle subclass that this handler, um, handles
 */
abstract public class SpanTagHandler <T> {
    /**
     * Convers an XHTML tag into an instance of the designated
     * CharacterStyle subclass, to apply to a Spanned
     *
     * @param name    the name of the tag, from the SAX parser
     * @param a       the attributes on the tag, from the SAX parser
     * @param context the value returned by findContextForTag()
     * @return an instance of the designated CharacterStyle subclass
     */
    public abstract T buildSpanForTag(String name, Attributes a, String context);
    
    /**
     * Returns the opening tag to use for representing the
     * supplied CharacterStyle instance in XHTML. This tag should
     * include all necessary attributes.
     *
     * @param span instance of a CharacterStyle to convert
     * @return the XHTML opening tag to use
     */
    public abstract String getStartTagForSpan(T span);
    
    /**
     * Returns the corresponding closing tag, which should line up
     * with whatever was returned by getStartTagForSpan().
     *
     * @param span instance of a CharacterStyle to convert
     * @return the XHTML closing tag to use
     */
    public abstract String getEndTagForSpan(T span);
    
    /**
     * Identifies the CharacterStyle subclass handled by this
     * handler.
     *
     * @return the Class object (subclass of CharacterStyle) that
     * is handled by this handler
     */
    public abstract Class getSupportedCharacterStyle();
    
    /**
     * Determines whether or not this handler is appropriate for
     * this tag. This is particularly important for tags that
     * might have multiple handlers (e.g., <span>), as the decision
     * of what handler handles the tag is made by attributes, not
     * just the tag name.
     *
     * @param name the name of the tag, from the SAX parser
     * @param a    the attributes on the tag, from the SAX parser
     * @return null if the handler does not handle this tag, or a String
     * to be passed to buildSpanForTag() when that is called
     * (useful for holding a group extracted via a regex)
     */
    public abstract String findContextForTag(String name, Attributes a);
    
    static final private ArrayList <SpanTagHandler> HANDLERS =
            new ArrayList <SpanTagHandler>();
    private static WeakHashMap <Class, SpanTagHandler> HANDLERS_BY_CHARACTER_STYLE =
            new WeakHashMap <Class, SpanTagHandler>();
    
    static {
        registerGlobalSpanTagHandler(new StyleSpanTagHandler());
        registerGlobalSpanTagHandler(new UnderlineSpanTagHandler());
        registerGlobalSpanTagHandler(new SuperscriptSpanTagHandler());
        registerGlobalSpanTagHandler(new SubscriptSpanTagHandler());
        registerGlobalSpanTagHandler(new StrikethroughSpanTagHandler());
        registerGlobalSpanTagHandler(new URLSpanTagHandler());
        registerGlobalSpanTagHandler(new ForegroundColorSpanTagHandler());
        registerGlobalSpanTagHandler(new BackgroundColorSpanTagHandler());
        registerGlobalSpanTagHandler(new AbsoluteSizeSpanTagHandler());
        registerGlobalSpanTagHandler(new RelativeSizeSpanTagHandler());
        registerGlobalSpanTagHandler(new TypefaceSizeSpanTagHandler());
    }
    
    static <T> SpanTagHandler getGlobalSpanTagHandler(Class <T> cls) {
        return (HANDLERS_BY_CHARACTER_STYLE.get(cls));
    }
    
    /**
     * Registers a process-scope SpanTagHandler. This will *not*
     * properly override any existing SpanTagHandler for the
     * same span/tag combination; use SpanTagRoster for those.
     * But, if you have *additional* spans and tags to handle,
     * and you want to do so globally, register them here.
     *
     * @param handler a SpanTagHandler to apply globally
     */
    public static void registerGlobalSpanTagHandler(SpanTagHandler handler) {
        HANDLERS.add(handler);
        HANDLERS_BY_CHARACTER_STYLE.put(handler.getSupportedCharacterStyle(), handler);
    }
    
    static Object buildSpanForTag(String name, Attributes a) {
        for (SpanTagHandler h : HANDLERS) {
            String context = h.findContextForTag(name, a);
            
            if (context != null) {
                return (h.buildSpanForTag(name, a, context));
            }
        }
        
        return (null);
    }
    
    /**
     * A partial implementation of SpanTagHandler that works for
     * plain conversions of simple XHTML tags to CharacterStyle
     * subclasses, without considering any attributes on the tags
     * or data members of the CharacterStyle.
     *
     * @param <T> the specific CharacterStyle subclass that this handler, um, handles
     */
    abstract public static class Simple <T>
            extends SpanTagHandler <T> {
        public abstract String[] getSupportedTags();
        
        private final String start;
        private final String end;
        
        /**
         * Constructor, as if you didn't already know that.
         *
         * @param start the XHTML opening tag (e.g., <foo>) that this handler handles
         * @param end   the XHTML closing tag (e.g., </foo>) that this handler handles
         */
        public Simple(String start, String end) {
            this.start = start;
            this.end = end;
        }
        
        @Override
        public String getStartTagForSpan(T span) {
            return (start);
        }
        
        @Override
        public String getEndTagForSpan(T span) {
            return (end);
        }
        
        @Override
        public String findContextForTag(String name, Attributes a) {
            for (String tag : getSupportedTags()) {
                if (tag.equals(name)) {
                    return (name);
                }
            }
            
            return (null);
        }
    }
}
