package app.simple.inure.decorations.padding;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.theme.ThemeFrameLayout;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.util.StatusBarHeight;

public class PaddingAwareFrameLayout extends ThemeFrameLayout {
    
    public PaddingAwareFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }
    
    public PaddingAwareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        if (AppearancePreferences.INSTANCE.isTransparentStatusDisabled()) {
            return;
        }
    
        setPadding(getPaddingLeft(),
                StatusBarHeight.getStatusBarHeight(getResources()) + getPaddingTop(),
                getPaddingRight(),
                getPaddingBottom());
    }
}
