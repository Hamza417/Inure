package app.simple.inure.decorations.accentlayouts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.themes.manager.Theme;

public class AccentCornerLinearLayout extends DynamicCornerLinearLayout {
    
    public AccentCornerLinearLayout(Context context) {
        super(context);
        init();
    }
    
    public AccentCornerLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public AccentCornerLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        
        setBackgroundTintList(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColor()));
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        // no-op
    }
}
