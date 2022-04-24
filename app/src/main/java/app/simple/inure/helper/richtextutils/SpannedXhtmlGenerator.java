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

import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.AlignmentSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;
import java.util.WeakHashMap;

/**
 * Generates XHTML from a Spanned, for use in persistence. Restore
 * the Spanned via SpannableStringGenerator.
 */
public class SpannedXhtmlGenerator {
    private static WeakHashMap <Class, SpanTagHandler> GLOBAL_SPAN_TAG_HANDLERS =
            new WeakHashMap <Class, SpanTagHandler>();
    private static final String[] CHUNK_SOURCES = {"\n\n", "\n"};
    private static final String[] CHUNK_REPLACEMENTS = {"</div><div>", "<br/>"};
    private final SpanTagRoster tagRoster;
    
    /**
     * Constructor, using a default SpanTagRoster.
     */
    public SpannedXhtmlGenerator() {
        this(new SpanTagRoster());
    }
    
    /**
     * Constructor. 'nuff said.
     *
     * @param tagRoster Rules for converting Spannables to/from XHTML
     */
    public SpannedXhtmlGenerator(SpanTagRoster tagRoster) {
        this.tagRoster = tagRoster;
    }
    
    /**
     * Generates XHTML from the supplied Spanned, according to the
     * rules from the SpanTagRoster provided in the constructor.
     *
     * @param src Spanned representing the rich text to convert
     * @return the XHTML generated from the Spanned
     */
    public String toXhtml(Spanned src) {
        SpannableStringBuilder result = new SpannableStringBuilder();
        AlignmentSpan[] spans = src.getSpans(0, src.length(), AlignmentSpan.class);
        
        if (spans.length == 0) {
            chunkToXhtml(result, src, null);
        } else {
            int lastSpanEnd = -1;
            
            for (AlignmentSpan span : spans) {
                int spanStart = src.getSpanStart(span);
                int spanEnd = src.getSpanEnd(span);
                
                if (spanStart > lastSpanEnd) {
                    if (spanStart > 0) {
                        int subsequenceStart = (lastSpanEnd < 0 ? 0 : lastSpanEnd);
                        
                        chunkToXhtml(result, subSequence(src, subsequenceStart, spanStart), null);
                    }
                }
                
                chunkToXhtml(result, subSequence(src, spanStart, spanEnd), span.getAlignment());
                
                lastSpanEnd = spanEnd;
            }
            
            if (lastSpanEnd < src.length()) {
                chunkToXhtml(result, subSequence(src, lastSpanEnd, src.length()), null);
            }
        }
        
        return (result.toString().replace("</div><div></", "</"));
    }
    
    private void chunkToXhtml(SpannableStringBuilder result, Spanned src,
            Layout.Alignment align) {
        BulletSpan[] spans = src.getSpans(0, src.length(), BulletSpan.class);
        
        if (spans.length == 0) {
            result.append(blockToXhtml(src, align));
        } else {
            int lastSpanEnd = -1;
            boolean inBulletRun = false;
            
            for (BulletSpan span : spans) {
                int spanStart = src.getSpanStart(span);
                int spanEnd = src.getSpanEnd(span);
                
                if (spanStart > lastSpanEnd) {
                    if (inBulletRun) {
                        result.append("</ul>");
                        inBulletRun = false;
                    }
                    
                    if (spanStart > 0) {
                        int subsequenceStart = (lastSpanEnd < 0 ? 0 : lastSpanEnd);
                        int subsequenceEnd = spanStart;
                        
                        if (src.charAt(spanStart) == '\n' && !inBulletRun) {
                            subsequenceEnd--; // to remove leading newline
                        }
                        
                        result.append(src.subSequence(subsequenceStart, subsequenceEnd));
                    }
                    
                    result.append("<ul");
                    result.append(buildAlignStyle(align));
                    result.append('>');
                    inBulletRun = true;
                }
                
                result.append("<li>");
                
                Spanned sub = (Spanned) src.subSequence(spanStart, spanEnd - 1);
                // -1 to remove trailing newline
                
                result.append(blockToXhtml(sub, null));
                result.append("</li>");
                
                lastSpanEnd = spanEnd;
            }
            
            if (inBulletRun) {
                result.append("</ul>");
            }
            
            if (lastSpanEnd < src.length()) {
                Spanned sub = (Spanned) src.subSequence(lastSpanEnd, src.length());
                
                result.append(blockToXhtml(sub, null));
            }
        }
    }
    
    private String buildAlignStyle(Layout.Alignment align) {
        StringBuilder buf = new StringBuilder();
        
        if (align != null) {
            buf.append(" style=\"text-align:");
            
            if (align == Layout.Alignment.ALIGN_CENTER) {
                buf.append("center");
            } else if (align == Layout.Alignment.ALIGN_NORMAL) {
                if (SpannedUtils.isRTL()) {
                    buf.append("right");
                } else {
                    buf.append("left");
                }
            } else {
                if (SpannedUtils.isRTL()) {
                    buf.append("left");
                } else {
                    buf.append("right");
                }
            }
            
            buf.append('"');
        }
        
        return (buf.toString());
    }
    
    private String blockToXhtml(Spanned src, Layout.Alignment align) {
        int blockStart = 0;
        int blockEnd = src.length();
        Stack <CharacterStyle> activeSpans = new Stack <CharacterStyle>();
        StringBuilder result = new StringBuilder(blockEnd - blockStart);
        
        for (int i = blockStart; i < blockEnd; ) {
            int nextSpanEnd = src.nextSpanTransition(i, src.length(),
                    CharacterStyle.class);
            CharacterStyle[] spansInEffect = src.getSpans(i, nextSpanEnd,
                    CharacterStyle.class);
            
            Arrays.sort(spansInEffect, new EndSpanComparator(src));
            
            while (!activeSpans.empty()) {
                boolean stillInEffect = false;
                CharacterStyle active = activeSpans.peek();
                
                for (CharacterStyle inEffect : spansInEffect) {
                    if (active == inEffect) {
                        stillInEffect = true;
                        break;
                    }
                }
                
                if (!stillInEffect) {
                    SpanTagHandler handler = tagRoster.getSpanTagHandler(active.getClass());
                    
                    if (handler != null) {
                        result.append(handler.getEndTagForSpan(active));
                    }
                    
                    activeSpans.pop();
                } else {
                    break;
                }
            }
            
            for (CharacterStyle inEffect : spansInEffect) {
                if (!activeSpans.contains(inEffect)) {
                    SpanTagHandler handler = tagRoster.getSpanTagHandler(inEffect.getClass());
                    
                    if (handler != null) {
                        result.append(handler.getStartTagForSpan(inEffect));
                    }
                    
                    activeSpans.push(inEffect);
                }
            }
            
            CharSequence chunk = src.subSequence(i, nextSpanEnd);
            
            while (hasAny(chunk, CHUNK_SOURCES)) {
                chunk = TextUtils.replace(chunk, CHUNK_SOURCES, CHUNK_REPLACEMENTS);
            }
            
            result.append(chunk);
            i = nextSpanEnd;
        }
        
        while (!activeSpans.empty()) {
            CharacterStyle active = activeSpans.pop();
            SpanTagHandler handler = tagRoster.getSpanTagHandler(active.getClass());
            
            if (handler != null) {
                result.append(handler.getEndTagForSpan(active));
            }
        }
        
        String baseResult = result.toString();
        
        result = new StringBuilder();
        
        if (baseResult.endsWith("</div><div>")) {
            result.append("<div");
            result.append(buildAlignStyle(align));
            result.append('>');
            result.append(baseResult.substring(0, baseResult.length() - 5));
        } else if (baseResult.contains("</div><div>") || align != null) {
            result.append("<div");
            result.append(buildAlignStyle(align));
            result.append('>');
            result.append(baseResult);
            result.append("</div>");
        } else {
            return (baseResult);
        }
        
        return (result.toString());
    }
    
    private static boolean hasAny(CharSequence input, String[] sources) {
        for (String source : sources) {
            if (TextUtils.indexOf(input, source) >= 0) {
                return (true);
            }
        }
        
        return (false);
    }
    
    private Spanned subSequence(CharSequence cs, int start, int end) {
        CharSequence sub = cs.subSequence(start, end);
        
        if (!(sub instanceof Spanned)) {
            sub = new SpannedString(sub);
        }
        
        return ((Spanned) sub);
    }
    
    private static class EndSpanComparator implements Comparator <Object> {
        final private Spanned src;
        
        EndSpanComparator(Spanned src) {
            this.src = src;
        }
        
        @Override
        public int compare(Object lhs, Object rhs) {
            return (src.getSpanEnd(rhs) - src.getSpanEnd(lhs));
        }
    }
}
