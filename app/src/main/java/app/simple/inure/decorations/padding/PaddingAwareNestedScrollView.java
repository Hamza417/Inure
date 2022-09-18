package app.simple.inure.decorations.padding;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.fastscroll.FastScrollNestedScrollView;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.util.StatusBarHeight;

public class PaddingAwareNestedScrollView extends FastScrollNestedScrollView implements SharedPreferences.OnSharedPreferenceChangeListener {
    public PaddingAwareNestedScrollView(@NonNull Context context) {
        super(context);
        init();
    }
    
    public PaddingAwareNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        updatePadding();
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
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
