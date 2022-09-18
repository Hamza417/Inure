package app.simple.inure.decorations.ripple;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.Objects;

import androidx.annotation.Nullable;
import app.simple.inure.preferences.AppearancePreferences;

public class DynamicRippleLinearLayout extends LinearLayout implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    public DynamicRippleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        setBackgroundColor(Color.TRANSPARENT);
        setBackground(null);
        setBackground(Utils.getRippleDrawable(getBackground()));
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) {
            return;
        }
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Objects.equals(key, AppearancePreferences.accentColor)) {
            init();
        }
    }
}