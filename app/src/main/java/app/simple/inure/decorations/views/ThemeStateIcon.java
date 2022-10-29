package app.simple.inure.decorations.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.R;
import app.simple.inure.constants.ThemeConstants;
import app.simple.inure.decorations.theme.ThemeIcon;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.themes.manager.Theme;

public class ThemeStateIcon extends ThemeIcon {
    
    public ThemeStateIcon(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ThemeStateIcon(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (!isInEditMode()) {
            updateIcon(false);
        }
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        super.onThemeChanged(theme, animate);
        updateIcon(true);
    }
    
    private void updateIcon(boolean animate) {
        switch (AppearancePreferences.INSTANCE.getTheme()) {
            case ThemeConstants.SOAPSTONE:
            case ThemeConstants.LIGHT_THEME: {
                setIcon(R.drawable.ic_light_mode, animate);
                break;
            }
            case ThemeConstants.OIL:
            case ThemeConstants.DARK_THEME: {
                setIcon(R.drawable.ic_dark_mode, animate);
                break;
            }
            case ThemeConstants.AMOLED: {
                setIcon(R.drawable.ic_dark_mode_amoled, animate);
                break;
            }
            case ThemeConstants.SLATE: {
                setIcon(R.drawable.ic_dark_mode_slate, animate);
                break;
            }
            case ThemeConstants.HIGH_CONTRAST: {
                setIcon(R.drawable.ic_dark_mode_high_contrast, animate);
                break;
            }
            case ThemeConstants.FOLLOW_SYSTEM: {
                setIcon(R.drawable.ic_android, animate);
                break;
            }
            case ThemeConstants.DAY_NIGHT: {
                setIcon(R.drawable.ic_hourglass_top, animate);
                break;
            }
        }
    }
}
