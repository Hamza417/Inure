package app.simple.inure.decorations.corners;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import app.simple.inure.R;
import app.simple.inure.preferences.AccessibilityPreferences;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.util.ColorUtils;

public class LayoutBackground {
    
    private static final float strokeWidth = 1F;
    
    public static void setBackground(Context context, ViewGroup viewGroup, AttributeSet attrs) {
        TypedArray theme = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DynamicCornerLayout, 0, 0);
        
        boolean roundTopCorners = theme.getBoolean(R.styleable.DynamicCornerLayout_roundTopCorners, false);
        boolean roundBottomCorners = theme.getBoolean(R.styleable.DynamicCornerLayout_roundBottomCorners, false);
        
        ShapeAppearanceModel shapeAppearanceModel;
        
        if (roundBottomCorners && roundTopCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .build();
        } else if (roundTopCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .setTopRightCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .build();
        } else if (roundBottomCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setBottomLeftCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .setBottomRightCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .build();
        } else {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .build();
        }
        
        viewGroup.setBackground(new MaterialShapeDrawable(shapeAppearanceModel));
    }
    
    public static void setBackground(Context context, View viewGroup, AttributeSet attrs, float factor) {
        TypedArray theme = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DynamicCornerLayout, 0, 0);
    
        boolean roundTopCorners = theme.getBoolean(R.styleable.DynamicCornerLayout_roundTopCorners, false);
        boolean roundBottomCorners = theme.getBoolean(R.styleable.DynamicCornerLayout_roundBottomCorners, false);
    
        ShapeAppearanceModel shapeAppearanceModel;
    
        if (roundBottomCorners && roundTopCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius() / factor)
                    .build();
        } else if (roundTopCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius() / factor)
                    .setTopRightCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius() / factor)
                    .build();
        } else if (roundBottomCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setBottomLeftCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius() / factor)
                    .setBottomRightCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius() / factor)
                    .build();
        } else {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius() / factor)
                    .build();
        }
    
        MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
    
        if (AccessibilityPreferences.INSTANCE.isHighlightStroke()) {
            materialShapeDrawable.setStroke(strokeWidth, ColorUtils.INSTANCE.resolveAttrColor(context, R.attr.colorAppAccent));
        }
    
        viewGroup.setBackground(materialShapeDrawable);
    }
    
    public static void setBackground(Context context, View view, AttributeSet attrs) {
        TypedArray theme = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DynamicCornerLayout, 0, 0);
        
        boolean roundTopCorners = theme.getBoolean(R.styleable.DynamicCornerLayout_roundTopCorners, false);
        boolean roundBottomCorners = theme.getBoolean(R.styleable.DynamicCornerLayout_roundBottomCorners, false);
        
        ShapeAppearanceModel shapeAppearanceModel;
        
        if (roundBottomCorners && roundTopCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .build();
        } else if (roundTopCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .setTopRightCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .build();
        } else if (roundBottomCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setBottomLeftCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .setBottomRightCorner(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .build();
        } else {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                    .build();
        }
    
        MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
    
        if (AccessibilityPreferences.INSTANCE.isHighlightStroke()) {
            materialShapeDrawable.setStroke(strokeWidth, ColorUtils.INSTANCE.resolveAttrColor(context, R.attr.colorAppAccent));
        }
    
        view.setBackground(materialShapeDrawable);
    }
    
    public static void setBackground(View view) {
        ShapeAppearanceModel shapeAppearanceModel;
        
        shapeAppearanceModel = new ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, AppearancePreferences.INSTANCE.getCornerRadius())
                .build();
        
        view.setBackground(new MaterialShapeDrawable(shapeAppearanceModel));
    }
}
