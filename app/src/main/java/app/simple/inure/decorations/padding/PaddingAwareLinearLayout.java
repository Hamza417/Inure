package app.simple.inure.decorations.padding;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import app.simple.inure.decorations.theme.ThemeLinearLayout;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.util.StatusBarHeight;

public class PaddingAwareLinearLayout extends ThemeLinearLayout {
    
    public PaddingAwareLinearLayout(Context context) {
        super(context);
        init();
    }
    
    public PaddingAwareLinearLayout(Context context, @Nullable AttributeSet attrs) {
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
