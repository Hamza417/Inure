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
        updateIcon();
    }
    
    public ThemeStateIcon(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        updateIcon();
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        super.onThemeChanged(theme, animate);
        updateIcon();
    }
    
    private void updateIcon() {
        switch (AppearancePreferences.INSTANCE.getTheme()) {
            case ThemeConstants.LIGHT_THEME: {
                setImageResource(R.drawable.ic_light_mode);
                break;
            }
            case ThemeConstants.DARK_THEME: {
                setImageResource(R.drawable.ic_dark_mode);
                break;
            }
            case ThemeConstants.AMOLED: {
                setImageResource(R.drawable.ic_dark_mode_amoled);
                break;
            }
            case ThemeConstants.SLATE: {
                setImageResource(R.drawable.ic_dark_mode_slate);
                break;
            }
            case ThemeConstants.HIGH_CONTRAST: {
                setImageResource(R.drawable.ic_dark_mode_high_contrast);
                break;
            }
            case ThemeConstants.FOLLOW_SYSTEM: {
                setImageResource(R.drawable.ic_android);
                break;
            }
            case ThemeConstants.DAY_NIGHT: {
                setImageResource(R.drawable.ic_hourglass_top);
                break;
            }
        }
    }
}
