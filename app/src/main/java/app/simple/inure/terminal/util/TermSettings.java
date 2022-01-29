/*
 * Copyright (C) 2007 The Android Open Source Project
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

package app.simple.inure.terminal.util;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.KeyEvent;

import app.simple.inure.R;
import app.simple.inure.preferences.ShellPreferences;
import app.simple.inure.preferences.TerminalPreferences;
import app.simple.inure.themes.manager.ThemeManager;

/**
 * Terminal emulator settings
 */
public class TermSettings {
    private SharedPreferences mPrefs;
    
    private int mActionBarMode;
    private int mOrientation;
    private int mCursorStyle;
    private String mHomePath;
    
    private String mPrependPath = null;
    private String mAppendPath = null;
    
    private static final String ACTIONBAR_KEY = "actionbar";
    private static final String ORIENTATION_KEY = "orientation";
    private static final String HOMEPATH_KEY = "home_path";
    
    public static final int WHITE = 0xffffffff;
    public static final int BLACK = 0xff000000;
    public static final int BLUE = 0xff344ebd;
    public static final int GREEN = 0xff00ff00;
    public static final int AMBER = 0xffffb651;
    public static final int RED = 0xffff0113;
    public static final int HOLO_BLUE = 0xff33b5e5;
    public static final int SOLARIZED_FG = 0xff657b83;
    public static final int SOLARIZED_BG = 0xfffdf6e3;
    public static final int SOLARIZED_DARK_FG = 0xff839496;
    public static final int SOLARIZED_DARK_BG = 0xff002b36;
    public static final int LINUX_CONSOLE_WHITE = 0xffaaaaaa;
    
    // foreground color, background color
    public static final int[][] COLOR_SCHEMES = {
            {ThemeManager.INSTANCE.getTheme().getTextViewTheme().getPrimaryTextColor(),
                    ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground()},
            {BLACK, WHITE},
            {WHITE, BLACK},
            {WHITE, BLUE},
            {GREEN, BLACK},
            {AMBER, BLACK},
            {RED, BLACK},
            {HOLO_BLUE, BLACK},
            {SOLARIZED_FG, SOLARIZED_BG},
            {SOLARIZED_DARK_FG, SOLARIZED_DARK_BG},
            {LINUX_CONSOLE_WHITE, BLACK}
    };
    
    public static final int ACTION_BAR_MODE_NONE = 0;
    public static final int ACTION_BAR_MODE_ALWAYS_VISIBLE = 1;
    public static final int ACTION_BAR_MODE_HIDES = 2;
    private static final int ACTION_BAR_MODE_MAX = 2;
    
    /**
     * An integer not in the range of real key codes.
     */
    public static final int KEYCODE_NONE = -1;
    
    public static final int CONTROL_KEY_ID_NONE = 7;
    public static final int[] CONTROL_KEY_SCHEMES = {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_AT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_ALT_RIGHT,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_CAMERA,
            KEYCODE_NONE
    };
    
    public static final int FN_KEY_ID_NONE = 7;
    public static final int[] FN_KEY_SCHEMES = {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_AT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_ALT_RIGHT,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_CAMERA,
            KEYCODE_NONE
    };
    
    public static final int BACK_KEY_STOPS_SERVICE = 0;
    public static final int BACK_KEY_CLOSES_WINDOW = 1;
    public static final int BACK_KEY_CLOSES_ACTIVITY = 2;
    public static final int BACK_KEY_SENDS_ESC = 3;
    public static final int BACK_KEY_SENDS_TAB = 4;
    private static final int BACK_KEY_MAX = 4;
    
    public TermSettings(Resources res, SharedPreferences prefs) {
        readDefaultPrefs(res);
        readPrefs(prefs);
    }
    
    private void readDefaultPrefs(Resources res) {
        mActionBarMode = res.getInteger(R.integer.pref_actionbar_default);
        mOrientation = res.getInteger(R.integer.pref_orientation_default);
        mCursorStyle = Integer.parseInt(res.getString(R.string.pref_cursorstyle_default));
        // the mHomePath default is set dynamically in readPrefs()
    }
    
    public void readPrefs(SharedPreferences prefs) {
        mPrefs = prefs;
        mActionBarMode = readIntPref(ACTIONBAR_KEY, mActionBarMode, ACTION_BAR_MODE_MAX);
        mOrientation = readIntPref(ORIENTATION_KEY, mOrientation, 2);
        // mCursorStyle = readIntPref(CURSORSTYLE_KEY, mCursorStyle, 2);
        // mCursorBlink = readIntPref(CURSORBLINK_KEY, mCursorBlink, 1);
        mHomePath = readStringPref(HOMEPATH_KEY, mHomePath);
        mPrefs = null;  // we leak a Context if we hold on to this
    }
    
    private int readIntPref(String key, int defaultValue, int maxValue) {
        int val;
        try {
            val = Integer.parseInt(
                    mPrefs.getString(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException e) {
            val = defaultValue;
        }
        val = Math.max(0, Math.min(val, maxValue));
        return val;
    }
    
    private String readStringPref(String key, String defaultValue) {
        return mPrefs.getString(key, defaultValue);
    }
    
    private boolean readBooleanPref(String key, boolean defaultValue) {
        return mPrefs.getBoolean(key, defaultValue);
    }
    
    public int actionBarMode() {
        return mActionBarMode;
    }
    
    public int getScreenOrientation() {
        return mOrientation;
    }
    
    public int getCursorStyle() {
        return mCursorStyle;
    }
    
    public int[] getColorScheme() {
        return COLOR_SCHEMES[TerminalPreferences.INSTANCE.getColor()];
    }
    
    public boolean backKeySendsCharacter() {
        return TerminalPreferences.INSTANCE.getBackButtonAction() >= BACK_KEY_SENDS_ESC;
    }
    
    public int getBackKeyCharacter() {
        switch (TerminalPreferences.INSTANCE.getBackButtonAction()) {
            case BACK_KEY_SENDS_ESC:
                return 27;
            case BACK_KEY_SENDS_TAB:
                return 9;
            default:
                return 0;
        }
    }
    
    public int getControlKeyCode() {
        return CONTROL_KEY_SCHEMES[TerminalPreferences.INSTANCE.getControlKey()];
    }
    
    public int getFnKeyCode() {
        return FN_KEY_SCHEMES[TerminalPreferences.INSTANCE.getFnKey()];
    }
    
    public String getShell() {
        return ShellPreferences.INSTANCE.getCommandLine();
    }
    
    public String getFailsafeShell() {
        return "/system/bin/sh -";
    }
    
    public void setPrependPath(String prependPath) {
        mPrependPath = prependPath;
    }
    
    public String getPrependPath() {
        return mPrependPath;
    }
    
    public void setAppendPath(String appendPath) {
        mAppendPath = appendPath;
    }
    
    public String getAppendPath() {
        return mAppendPath;
    }
}
