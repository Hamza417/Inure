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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Arrays;
import java.util.Stack;

class XhtmlSaxHandler extends DefaultHandler {
  private static final String[] NO_ITEM_TAGS = {"br", "div"};
  private final SpanTagRoster tagRoster;
  private Stack <Item> textStack = new Stack <Item>();
  boolean isInBulletedList = false;
  
  XhtmlSaxHandler(SpanTagRoster tagRoster) {
    this.tagRoster = tagRoster;
    
    textStack.push(new Item(null, ""));
  }
  
  Spannable getContent() {
    return (textStack.peek().getContent());
  }
  
  public void startElement(String uri, String localName,
          String name, Attributes a) {
    String style = a.getValue("style");
    
    if ("ul".equals(name)) {
      isInBulletedList = true;
      
      Item item = textStack.peek();
      
      if (item.isCharacterStyle()) {
        item.append("\n");
      }
      
      if (style != null) {
        handleAlignment(style);
      }
    } else if ("li".equals(name)) {
      if (isInBulletedList) {
        textStack.push(new Item(new BulletSpan(), ""));
      } else {
        throw new IllegalStateException("Found <li> without enclosing <ul>");
      }
    } else if ("div".equals(name) && style != null) {
      handleAlignment(style);
    } else if (Arrays.binarySearch(NO_ITEM_TAGS, name) <= 0) {
      Object span = tagRoster.buildSpanForTag(name, a);
      
      textStack.push(new Item(span, ""));
    }
  }
  
  public void endElement(String uri, String localName, String name) {
    if ("div".equals(name)) {
      textStack.peek().append("\n\n");
    } else if ("br".equals(name)) {
      textStack.peek().append("\n");
    } else if ("ul".equals(name)) {
      isInBulletedList = false;
    } else {
      if ("li".equals(name)) {
        textStack.peek().append("\n");
      }
      
      Item toFinish = textStack.pop();
      Item theNewTop = textStack.peek();
      
      theNewTop.append(toFinish);
    }
  }
  
  public void characters(char[] ch, int start, int length) {
    textStack.peek().append(new String(ch, start, length));
  }
  
  public InputSource resolveEntity(String publicId, String systemId)
          throws org.xml.sax.SAXException, java.io.IOException {
    throw new IllegalStateException("Entities are not supported!");
  }
  
  private void handleAlignment(String style) {
    if (style.startsWith("text-align:")) {
      Layout.Alignment align = translateTextAlign(style.substring(11));
      
      textStack.push(new Item(new AlignmentSpan.Standard(align), ""));
    } else {
      throw new IllegalStateException("Unrecognized <div> style: " + style);
    }
  }
  
  private Layout.Alignment translateTextAlign(String textAlign) {
    if ("center".equals(textAlign)) {
      return (Layout.Alignment.ALIGN_CENTER);
    } else if ("left".equals(textAlign)) {
      return (SpannedUtils.isRTL() ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_NORMAL);
    }
    
    return (SpannedUtils.isRTL() ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE);
  }
  
  private static class Item {
    private final Object activeSpan;
    private final SpannableStringBuilder content;
    
    Item(Object span, CharSequence initialContent) {
      activeSpan = span;
      content = new SpannableStringBuilder(initialContent);
    }
    
    void append(CharSequence newContent) {
      content.append(newContent);
    }
    
    void append(Item item) {
      int start = content.length();
      
      content.append(item.content);
      
      if (item.activeSpan != null) {
        content.setSpan(item.activeSpan, start, content.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
      }
    }
    
    Spannable getContent() {
      return (content);
    }
    
    boolean isCharacterStyle() {
      return (activeSpan != null && activeSpan instanceof CharacterStyle);
    }
  }
}
