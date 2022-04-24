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

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.CharacterStyle;

import java.util.Locale;

public class SpannedUtils {
  public static <A extends CharacterStyle, B extends CharacterStyle> Spannable replaceAll(Spanned original,
          Class <A> sourceType,
          SpanConverter <A, B> converter) {
    SpannableString result = new SpannableString(original);
    A[] spans = result.getSpans(0, result.length(), sourceType);
    
    for (A span : spans) {
      int start = result.getSpanStart(span);
      int end = result.getSpanEnd(span);
      int flags = result.getSpanFlags(span);
      
      result.removeSpan(span);
      result.setSpan(converter.convert(span), start, end, flags);
    }
    
    return (result);
  }
  
  // based on http://stackoverflow.com/a/23203698/115145
  
  public static boolean isRTL() {
    return isRTL(Locale.getDefault());
  }
  
  public static boolean isRTL(Locale locale) {
    final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
    return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
            directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
  }
  
  public interface SpanConverter <A extends CharacterStyle, B extends CharacterStyle> {
    B convert(A span);
  }
}
