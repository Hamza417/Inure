package app.simple.inure.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.preferences.SharedPreferences;
import app.simple.inure.util.ViewUtils;

public class DynamicCornerAccentColor extends FrameLayout implements android.content.SharedPreferences.OnSharedPreferenceChangeListener {
    public DynamicCornerAccentColor(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(null);
    }
    
    public DynamicCornerAccentColor(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attributeSet) {
        if (!isInEditMode()) {
            LayoutBackground.setBackground(getContext(), this, attributeSet, 1.4F);
            ViewUtils.INSTANCE.addShadow(this);
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onSharedPreferenceChanged(android.content.SharedPreferences sharedPreferences, @Nullable String s) {
        if (s.equals(AppearancePreferences.accentColor)) {
            LayoutBackground.setBackground(getContext(), this, null, 1.4F);
        }
    }
}
