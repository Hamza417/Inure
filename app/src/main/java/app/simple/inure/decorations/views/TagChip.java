package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;

import com.google.android.material.chip.Chip;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import app.simple.inure.R;
import app.simple.inure.preferences.AccessibilityPreferences;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.TypeFace;
import app.simple.inure.util.ViewUtils;

public class TagChip extends Chip {
    
    public TagChip(Context context) {
        super(context);
        init();
    }
    
    public TagChip(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public TagChip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setTypeface(TypeFace.INSTANCE.getMediumTypeFace(getContext()));
        setTextColor(ColorStateList
                .valueOf(ThemeManager.INSTANCE.getTheme().getTextViewTheme().getPrimaryTextColor()));
        
        setChipIconSize(getResources().getDimensionPixelSize(R.dimen.chip_icon_size));
        setChipIconTint(ColorStateList
                .valueOf(ThemeManager.INSTANCE.getTheme().getIconTheme().getRegularIconColor()));
        
        setChipBackgroundColor(new ColorStateList(new int[][] {
                new int[] {
                        android.R.attr.state_checked
                },
                new int[] {
                
                }},
                new int[] {
                        AppearancePreferences.INSTANCE.getAccentColor(), // Ripple color
                        ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getHighlightBackground() // Background color
                }
        ));
        
        setShapeAppearanceModel(new ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                .build());
        
        ViewUtils.INSTANCE.addShadow(this);
        setRippleColor(ColorStateList
                .valueOf(AppearancePreferences.INSTANCE.getAccentColorLight(getContext())));
        
        if (AccessibilityPreferences.INSTANCE.isHighlightStroke()) {
            setChipStrokeColor(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColor()));
            setChipStrokeWidth(1);
        }
    }
    
    public void setChipColor(int color, boolean whiteText) {
        setChipBackgroundColor(new ColorStateList(new int[][] {
                new int[] {
                        android.R.attr.state_checked
                },
                new int[] {
                
                }},
                new int[] {
                        color, // Ripple color
                        color // Background color
                }
        ));
        
        if (whiteText) {
            setTextColor(ColorStateList.valueOf(Color.WHITE));
            setChipIconTint(ColorStateList.valueOf(Color.WHITE));
            setRippleColor(ColorStateList.valueOf(Color.WHITE));
        } else {
            setTextColor(ColorStateList
                    .valueOf(ThemeManager.INSTANCE.getTheme()
                            .getTextViewTheme().getPrimaryTextColor()));
            
            setChipIconTint(ColorStateList
                    .valueOf(ThemeManager.INSTANCE.getTheme()
                            .getIconTheme().getRegularIconColor()));
            
            setRippleColor(ColorStateList
                    .valueOf(AppearancePreferences.INSTANCE.getAccentColorLight(getContext())));
        }
    }
    
    public void setDefaultChipColor() {
        setChipBackgroundColor(new ColorStateList(new int[][] {
                new int[] {
                        android.R.attr.state_checked
                },
                new int[] {
                
                }},
                new int[] {
                        AppearancePreferences.INSTANCE.getAccentColor(), // Ripple color
                        ThemeManager.INSTANCE.getTheme()
                                .getViewGroupTheme().getViewerBackground() // Background color
                }
        ));
        
        setTextColor(ColorStateList
                .valueOf(ThemeManager.INSTANCE.getTheme()
                        .getTextViewTheme().getPrimaryTextColor()));
        
        setChipIconTint(ColorStateList
                .valueOf(ThemeManager.INSTANCE.getTheme()
                        .getIconTheme().getRegularIconColor()));
        
        setRippleColor(ColorStateList
                .valueOf(AppearancePreferences.INSTANCE.getAccentColorLight(getContext())));
    }
}
