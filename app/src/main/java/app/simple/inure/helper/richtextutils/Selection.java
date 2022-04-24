/***
 Copyright (c) 2008-2011 CommonsWare, LLC
 
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

import android.text.TextUtils;
import android.widget.EditText;

import java.util.ArrayList;

public class Selection {
    int start;
    int end;
    
    public Selection(int _start, int _end) {
        start = _start;
        end = _end;
        
        if (start > end) {
            int temp = end;
            end = start;
            start = temp;
        }
    }
    
    public Selection(EditText editor) {
        this(editor.getSelectionStart(), editor.getSelectionEnd());
    }
    
    public int getStart() {
        return (start);
    }
    
    public int getEnd() {
        return (end);
    }
    
    public boolean isEmpty() {
        return (start == end);
    }
    
    public void apply(EditText editor) {
        editor.setSelection(start, end);
    }
    
    public Selection extendToFullLine(CharSequence src) {
        int newStart;
        int newEnd;
        
        for (newStart = start; newStart > 0; newStart--) {
            if (src.charAt(newStart - 1) == '\n') {
                break;
            }
        }
        
        for (newEnd = end; newEnd < src.length() - 1; newEnd++) {
            if (src.charAt(newEnd + 1) == '\n') {
                break;
            }
        }
        
        return (new Selection(newStart, newEnd));
    }
    
    // must already be extended to full line
    public ArrayList <Selection> buildSelectionsForLines(CharSequence src) {
        ArrayList <Selection> results = new ArrayList <Selection>();
        int chunkStart = start;
        int newline;
        
        while ((newline = TextUtils.indexOf(src, '\n', chunkStart, end)) > -1) {
            results.add(new Selection(chunkStart, newline));
            chunkStart = newline + 1;
        }
        
        results.add(new Selection(chunkStart, end));
        
        return (results);
    }
}