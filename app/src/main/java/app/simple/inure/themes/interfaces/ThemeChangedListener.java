package app.simple.inure.themes.interfaces;

import app.simple.inure.themes.manager.Theme;

public interface ThemeChangedListener {
    default void onThemeChanged(Theme theme) {
    
    }
}
