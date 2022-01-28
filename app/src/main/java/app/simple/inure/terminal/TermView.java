/*
 * Copyright (C) 2012 Steven Luo
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

package app.simple.inure.terminal;

import android.content.Context;
import android.util.DisplayMetrics;

import app.simple.inure.decorations.emulatorview.ColorScheme;
import app.simple.inure.decorations.emulatorview.EmulatorView;
import app.simple.inure.decorations.emulatorview.TermSession;
import app.simple.inure.preferences.TerminalPreferences;
import app.simple.inure.terminal.util.TermSettings;
import app.simple.inure.themes.manager.ThemeManager;

public class TermView extends EmulatorView {
    
    public TermView(Context context, TermSession session, DisplayMetrics metrics) {
        super(context, session, metrics);
    }
    
    public void updatePrefs(TermSettings settings, ColorScheme scheme) {
        if (scheme == null) {
            scheme = new ColorScheme(settings.getColorScheme());
        }
    
        setTextSize(TerminalPreferences.INSTANCE.getFontSize());
        setUseCookedIME(settings.useCookedIME());
        setColorScheme(scheme);
        if (TerminalPreferences.INSTANCE.getColor() == 0) {
            setColorScheme(new ColorScheme(ThemeManager.INSTANCE.getTheme().getTextViewTheme().getPrimaryTextColor(),
                    ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground()));
        } else {
            setColorScheme(scheme);
        }
        setBackKeyCharacter(settings.getBackKeyCharacter());
        setAltSendsEsc(TerminalPreferences.INSTANCE.getAltKeyEscapeState());
        setControlKeyCode(settings.getControlKeyCode());
        setFnKeyCode(settings.getFnKeyCode());
        setTermType(settings.getTermType());
        setMouseTracking(settings.getMouseTrackingFlag());
    
        System.out.println("Called Emu View");
    }
    
    public void updatePrefs(TermSettings settings) {
        updatePrefs(settings, null);
    }
    
    @Override
    public String toString() {
        return getClass().toString() + '(' + getTermSession() + ')';
    }
}
