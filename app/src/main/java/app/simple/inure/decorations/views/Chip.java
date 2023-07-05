package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import app.simple.inure.R;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.preferences.DevelopmentPreferences;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.TypeFace;
import app.simple.inure.util.ViewUtils;

public class Chip extends com.google.android.material.chip.Chip {
    
    public Chip(Context context) {
        super(context);
        init();
    }
    
    public Chip(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public Chip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setCheckable(true);
        
        if (isInEditMode()) {
            return;
        }
        
        setCheckedIconResource(R.drawable.ic_dot_mini);
        setCheckedIconTint(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColor()));
        setTypeface(TypeFace.INSTANCE.getMediumTypeFace(getContext()));
        setTextColor(ColorStateList.valueOf(ThemeManager.INSTANCE.getTheme().getTextViewTheme().getPrimaryTextColor()));
        setChipBackgroundColor(new ColorStateList(new int[][] {
                new int[] {
                        android.R.attr.state_checked
                },
                new int[] {
                
                }},
                new int[] {
                        AppearancePreferences.INSTANCE.getAccentColorLight(getContext()),
                        ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getViewerBackground()
                }
        ));
    
        setShapeAppearanceModel(new ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                .build());
    
        ViewUtils.INSTANCE.addShadow(this);
        setRippleColor(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColorLight(getContext())));
    
        if (!DevelopmentPreferences.INSTANCE.get(DevelopmentPreferences.removeStrokeFromChips)) {
            setChipStrokeColor(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColor()));
            setChipStrokeWidth(1);
        }
    }
}
