package app.simple.inure.decorations.padding;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.theme.ThemeFrameLayout;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.util.StatusBarHeight;

public class PaddingAwareFrameLayout extends ThemeFrameLayout implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    public PaddingAwareFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }
    
    public PaddingAwareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        updatePadding();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    private void updatePadding() {
        if (AppearancePreferences.INSTANCE.isTransparentStatusDisabled()) {
            if (getPaddingTop() >= StatusBarHeight.getStatusBarHeight(getResources())) {
                setPadding(getPaddingLeft(),
                        Math.abs(StatusBarHeight.getStatusBarHeight(getResources()) - getPaddingTop()),
                        getPaddingRight(),
                        getPaddingBottom());
            }
        } else {
            setPadding(getPaddingLeft(),
                    StatusBarHeight.getStatusBarHeight(getResources()) + getPaddingTop(),
                    getPaddingRight(),
                    getPaddingBottom());
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(AppearancePreferences.transparentStatus)) {
            updatePadding();
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
