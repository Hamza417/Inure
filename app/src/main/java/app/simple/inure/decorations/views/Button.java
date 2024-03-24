package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.TypeFace;

public class Button extends MaterialButton implements ThemeChangedListener {
    public Button(@NonNull Context context) {
        super(context);
        init();
    }
    
    public Button(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public Button(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (!isInEditMode()) {
            setRippleColor(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColor()));
            setTypeface(TypeFace.INSTANCE.getMediumTypeFace(getContext()));
            setStrokeColor(ColorStateList.valueOf(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getDividerBackground()));
            setStrokeWidth(1);
            setElevation(0);
            
            int cornerRadius = (int) AppearancePreferences.INSTANCE.getCornerRadius();
            if (cornerRadius > 25) {
                cornerRadius = 25;
            }
            setCornerRadius(cornerRadius);
            
            setBackgroundTintList(new ColorStateList(new int[][] {
                    new int[] {-android.R.attr.state_checked}, // This is for the unchecked state
                    new int[] {android.R.attr.state_enabled}, // This is for the enabled state
                    new int[] {-android.R.attr.state_enabled} // This is for the disabled state
            },
                    new int[] {
                            ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground(), // Color for the unchecked state
                            AppearancePreferences.INSTANCE.getAccentColor(), // Color for the enabled state
                            ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground() // Color for the disabled state
                    }));
            
            setTextColor(new ColorStateList(new int[][] {
                    new int[] {-android.R.attr.state_checked}, // This is for the unchecked state
                    new int[] {android.R.attr.state_enabled}, // This is for the enabled state
                    new int[] {-android.R.attr.state_enabled} // This is for the disabled state
            },
                    new int[] {
                            ThemeManager.INSTANCE.getTheme().getTextViewTheme().getPrimaryTextColor(), // Color for the unchecked state
                            Color.WHITE, // Color for the enabled state
                            ThemeManager.INSTANCE.getTheme().getTextViewTheme().getQuaternaryTextColor() // Color for the disabled state
                    }));
        }
        
        setTextSize(10);
        setAllCaps(false);
    }
    
    public void setButtonCheckedColor(int color) {
        setBackgroundTintList(new ColorStateList(new int[][] {
                new int[] {-android.R.attr.state_checked}, // This is for the unchecked state
                new int[] {android.R.attr.state_enabled}, // This is for the enabled state
                new int[] {-android.R.attr.state_enabled} // This is for the disabled state
        },
                new int[] {
                        ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground(), // Color for the unchecked state
                        color, // Color for the enabled state
                        ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground() // Color for the disabled state
                }));
        
        invalidate();
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThemeManager.INSTANCE.addListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThemeManager.INSTANCE.removeListener(this);
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        ThemeChangedListener.super.onThemeChanged(theme, animate);
        init();
        postInvalidate();
    }
}
