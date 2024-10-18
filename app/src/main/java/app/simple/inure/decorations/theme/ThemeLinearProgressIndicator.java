package app.simple.inure.decorations.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Accent;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;

public class ThemeLinearProgressIndicator extends LinearProgressIndicator implements ThemeChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    
    public ThemeLinearProgressIndicator(@NonNull Context context) {
        super(context);
        init();
    }
    
    public ThemeLinearProgressIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ThemeLinearProgressIndicator(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        
        setIndicatorColor(AppearancePreferences.INSTANCE.getAccentColor());
        setTrackColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground());
        setTrackCornerRadius(10);
        setTrackThickness(5);
        setShowAnimationBehavior(SHOW_INWARD);
        setHideAnimationBehavior(HIDE_OUTWARD);
        invalidate();
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (key) {
            case AppearancePreferences.ACCENT_COLOR:
                init();
                break;
        }
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        ThemeChangedListener.super.onThemeChanged(theme, animate);
        init();
    }
    
    @Override
    public void onAccentChanged(@NonNull Accent accent) {
        ThemeChangedListener.super.onAccentChanged(accent);
        init();
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.registerSharedPreferencesListener(this);
        ThemeManager.INSTANCE.addListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.unregisterSharedPreferenceChangeListener(this);
        ThemeManager.INSTANCE.removeListener(this);
    }
}
