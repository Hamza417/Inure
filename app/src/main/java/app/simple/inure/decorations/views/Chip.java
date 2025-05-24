package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import app.simple.inure.R;
import app.simple.inure.preferences.AccessibilityPreferences;
import app.simple.inure.preferences.AppearancePreferences;
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
        
        setCheckedIconResource(R.drawable.ic_dot_12dp);
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
                        ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getHighlightBackground()
                }
        ));
        
        setShapeAppearanceModel(new ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius() / 2)
                .build());
        
        ViewUtils.INSTANCE.addShadow(this);
        setRippleColor(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColorLight(getContext())));
        
        if (AccessibilityPreferences.INSTANCE.isHighlightStroke()) {
            setChipStrokeColor(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColor()));
            setChipStrokeWidth(1);
        }
    }
    
    public void setChipBackgroundColor(int color) {
        setChipBackgroundColor(ColorStateList.valueOf(color));
    }
    
    public void setCheckedIconTint(int color) {
        setCheckedIconTint(ColorStateList.valueOf(color));
    }
    
    public void setChipStrokeColor(int color) {
        setChipStrokeColor(ColorStateList.valueOf(color));
    }
    
    public void setTextColor(int color) {
        setTextColor(ColorStateList.valueOf(color));
    }
    
    public void setRippleColor(int color) {
        setRippleColor(ColorStateList.valueOf(color));
    }
    
    public void setCornerRadius(float radius) {
        setShapeAppearanceModel(new ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build());
    }
    
    public void setIcon(int icon) {
        setCheckedIconResource(icon);
    }
    
    public void useRegularTypeface() {
        setTypeface(TypeFace.INSTANCE.getRegularTypeFace(getContext()));
    }
}
