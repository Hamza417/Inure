package app.simple.inure.decorations.padding;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import app.simple.inure.decorations.theme.ThemeConstraintLayout;
import app.simple.inure.util.ViewUtils;

public class PaddingAwareConstraintLayout extends ThemeConstraintLayout implements SharedPreferences.OnSharedPreferenceChangeListener {
    public PaddingAwareConstraintLayout(Context context) {
        super(context);
        init();
    }
    
    public PaddingAwareConstraintLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        ViewUtils.INSTANCE.paddingEdgeToEdge(this);
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
