/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.simple.inure.decorations.terminal;

import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;

public class TerminalKeys {
    // Taken from vterm_input.h
    // TODO: Consider setting these via jni
    public static final int VTERM_KEY_NONE = 0;
    public static final int VTERM_KEY_ENTER = 1;
    public static final int VTERM_KEY_TAB = 2;
    public static final int VTERM_KEY_BACKSPACE = 3;
    public static final int VTERM_KEY_ESCAPE = 4;
    public static final int VTERM_KEY_UP = 5;
    public static final int VTERM_KEY_DOWN = 6;
    public static final int VTERM_KEY_LEFT = 7;
    public static final int VTERM_KEY_RIGHT = 8;
    public static final int VTERM_KEY_INS = 9;
    public static final int VTERM_KEY_DEL = 10;
    public static final int VTERM_KEY_HOME = 11;
    public static final int VTERM_KEY_END = 12;
    public static final int VTERM_KEY_PAGEUP = 13;
    public static final int VTERM_KEY_PAGEDOWN = 14;
    public static final int VTERM_KEY_FUNCTION_0 = 256;
    public static final int VTERM_KEY_FUNCTION_MAX = VTERM_KEY_FUNCTION_0 + 255;
    public static final int VTERM_KEY_KP_0 = 512;
    public static final int VTERM_KEY_KP_1 = 513;
    public static final int VTERM_KEY_KP_2 = 514;
    public static final int VTERM_KEY_KP_3 = 515;
    public static final int VTERM_KEY_KP_4 = 516;
    public static final int VTERM_KEY_KP_5 = 517;
    public static final int VTERM_KEY_KP_6 = 518;
    public static final int VTERM_KEY_KP_7 = 519;
    public static final int VTERM_KEY_KP_8 = 520;
    public static final int VTERM_KEY_KP_9 = 521;
    public static final int VTERM_KEY_KP_MULT = 522;
    public static final int VTERM_KEY_KP_PLUS = 523;
    public static final int VTERM_KEY_KP_COMMA = 524;
    public static final int VTERM_KEY_KP_MINUS = 525;
    public static final int VTERM_KEY_KP_PERIOD = 526;
    public static final int VTERM_KEY_KP_DIVIDE = 527;
    public static final int VTERM_KEY_KP_ENTER = 528;
    public static final int VTERM_KEY_KP_EQUAL = 529;
    public static final int VTERM_MOD_NONE = 0x00;
    public static final int VTERM_MOD_SHIFT = 0x01;
    public static final int VTERM_MOD_ALT = 0x02;
    public static final int VTERM_MOD_CTRL = 0x04;
    private static final String TAG = "TerminalKeys";
    private static final boolean DEBUG = true;
    private Terminal mTerm;
    
    public static int getModifiers(KeyEvent event) {
        int mod = 0;
        if (event.isCtrlPressed()) {
            mod |= VTERM_MOD_CTRL;
        }
        if (event.isAltPressed()) {
            mod |= VTERM_MOD_ALT;
        }
        if (event.isShiftPressed()) {
            mod |= VTERM_MOD_SHIFT;
        }
        return mod;
    }
    
    public static int getKey(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_ENTER:
                return VTERM_KEY_ENTER;
            case KeyEvent.KEYCODE_TAB:
                return VTERM_KEY_TAB;
            case KeyEvent.KEYCODE_DEL:
                return VTERM_KEY_BACKSPACE;
            case KeyEvent.KEYCODE_ESCAPE:
                return VTERM_KEY_ESCAPE;
            case KeyEvent.KEYCODE_DPAD_UP:
                return VTERM_KEY_UP;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return VTERM_KEY_DOWN;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return VTERM_KEY_LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return VTERM_KEY_RIGHT;
            case KeyEvent.KEYCODE_INSERT:
                return VTERM_KEY_INS;
            case KeyEvent.KEYCODE_FORWARD_DEL:
                return VTERM_KEY_DEL;
            case KeyEvent.KEYCODE_MOVE_HOME:
                return VTERM_KEY_HOME;
            case KeyEvent.KEYCODE_MOVE_END:
                return VTERM_KEY_END;
            case KeyEvent.KEYCODE_PAGE_UP:
                return VTERM_KEY_PAGEUP;
            case KeyEvent.KEYCODE_PAGE_DOWN:
                return VTERM_KEY_PAGEDOWN;
            default:
                return 0;
        }
    }
    
    public static String getKeyName(int key) {
        switch (key) {
            case VTERM_KEY_ENTER:
                return "VTERM_KEY_ENTER";
            case VTERM_KEY_TAB:
                return "VTERM_KEY_TAB";
            case VTERM_KEY_BACKSPACE:
                return "VTERM_KEY_BACKSPACE";
            case VTERM_KEY_ESCAPE:
                return "VTERM_KEY_ESCAPE";
            case VTERM_KEY_UP:
                return "VTERM_KEY_UP";
            case VTERM_KEY_DOWN:
                return "VTERM_KEY_DOWN";
            case VTERM_KEY_LEFT:
                return "VTERM_KEY_LEFT";
            case VTERM_KEY_RIGHT:
                return "VTERM_KEY_RIGHT";
            case VTERM_KEY_INS:
                return "VTERM_KEY_INS";
            case VTERM_KEY_DEL:
                return "VTERM_KEY_DEL";
            case VTERM_KEY_HOME:
                return "VTERM_KEY_HOME";
            case VTERM_KEY_END:
                return "VTERM_KEY_END";
            case VTERM_KEY_PAGEUP:
                return "VTERM_KEY_PAGEUP";
            case VTERM_KEY_PAGEDOWN:
                return "VTERM_KEY_PAGEDOWN";
            case VTERM_KEY_NONE:
                return "VTERM_KEY_NONE";
            default:
                return "UNKNOWN KEY";
        }
    }
    
    public int getCharacter(KeyEvent event) {
        int c = event.getUnicodeChar();
        // TODO: Actually support dead keys
        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            Log.w(TAG, "Received dead key, ignoring");
            return 0;
        }
        return c;
    }
    
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (mTerm == null || event.getAction() == KeyEvent.ACTION_UP) {
            return false;
        }
        
        int modifiers = getModifiers(event);
        
        int c = getKey(event);
        if (c != 0) {
            if (DEBUG) {
                Log.d(TAG, "dispatched key event: " +
                        "mod=" + modifiers + ", " +
                        "keys=" + getKeyName(c));
            }
            return mTerm.dispatchKey(modifiers, c);
        }
        
        c = getCharacter(event);
        if (c != 0) {
            if (DEBUG) {
                Log.d(TAG, "dispatched key event: " +
                        "mod=" + modifiers + ", " +
                        "character='" + new String(Character.toChars(c)) + "'");
            }
            return mTerm.dispatchCharacter(modifiers, c);
        }
        
        return false;
    }
    
    public void setTerminal(Terminal term) {
        mTerm = term;
    }
}
