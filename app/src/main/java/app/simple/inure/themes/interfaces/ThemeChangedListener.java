package app.simple.inure.themes.interfaces;

import androidx.annotation.NonNull;
import app.simple.inure.themes.manager.Theme;

public interface ThemeChangedListener {
    default void onThemeChanged(@NonNull Theme theme, boolean animate) {
    
    }
}
