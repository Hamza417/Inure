package app.simple.inure.decorations.ripple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import app.simple.inure.R;
import app.simple.inure.constants.Misc;
import app.simple.inure.decorations.corners.LayoutBackground;
import app.simple.inure.decorations.typeface.TypeFaceTextView;
import app.simple.inure.preferences.AccessibilityPreferences;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.preferences.DevelopmentPreferences;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;

/**
 * {@link androidx.appcompat.widget.AppCompatTextView} but with animated
 * background
 */
public class DynamicRippleTextView extends TypeFaceTextView {
    
    private SpringAnimation springAnimationX;
    private SpringAnimation springAnimationY;
    
    public DynamicRippleTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
    }
    
    public DynamicRippleTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.TRANSPARENT);
    }
    
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        setHighlightBackgroundColor();
        super.setOnClickListener(l);
    }
    
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setBackground(Utils.getRoundedBackground(Misc.roundedCornerFactor));
        setClickable(false);
        setSelectedBackgroundColor();
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (AccessibilityPreferences.INSTANCE.isHighlightMode() && isClickable()) {
                    animate()
                            .scaleY(0.8F)
                            .scaleX(0.8F)
                            .alpha(0.5F)
                            .setInterpolator(new LinearOutSlowInInterpolator())
                            .setDuration(getResources().getInteger(R.integer.animation_duration))
                            .start();
                }
    
                try {
                    if (event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (isLongClickable()) {
                                if (event.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                                    performLongClick();
                                    return true;
                                } else {
                                    return super.onTouchEvent(event);
                                }
                            } else {
                                return super.onTouchEvent(event);
                            }
                        } else {
                            return super.onTouchEvent(event);
                        }
                    } else {
                        return super.onTouchEvent(event);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return super.onTouchEvent(event);
                }
            }
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (AccessibilityPreferences.INSTANCE.isHighlightMode() && isClickable()) {
                    animate()
                            .scaleY(1F)
                            .scaleX(1F)
                            .alpha(1F)
                            .setStartDelay(50)
                            .setInterpolator(new LinearOutSlowInInterpolator())
                            .setDuration(getResources().getInteger(R.integer.animation_duration))
                            .start();
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        // Animate the view on mouse hover
        if (!AccessibilityPreferences.INSTANCE.isAnimationReduced()) {
            if (DevelopmentPreferences.INSTANCE.get(DevelopmentPreferences.hoverAnimation)) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                    if (springAnimationX != null) {
                        springAnimationX.cancel();
                    }
                    
                    if (springAnimationY != null) {
                        springAnimationY.cancel();
                    }
                    
                    springAnimationX = new SpringAnimation(this, SpringAnimation.SCALE_X)
                            .setStartValue(getScaleX())
                            .setSpring(new SpringForce(Misc.hoverAnimationScaleOnHover)
                                    .setDampingRatio(Misc.hoverAnimationDampingRatio)
                                    .setStiffness(Misc.hoverAnimationStiffness));
                    
                    springAnimationY = new SpringAnimation(this, SpringAnimation.SCALE_Y)
                            .setStartValue(getScaleY())
                            .setSpring(new SpringForce(Misc.hoverAnimationScaleOnHover)
                                    .setDampingRatio(Misc.hoverAnimationDampingRatio)
                                    .setStiffness(Misc.hoverAnimationStiffness));
                    
                    springAnimationX.start();
                    springAnimationY.start();
                } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                    if (springAnimationX != null) {
                        springAnimationX.cancel();
                    }
                    
                    if (springAnimationY != null) {
                        springAnimationY.cancel();
                    }
                    
                    springAnimationX = new SpringAnimation(this, SpringAnimation.SCALE_X)
                            .setStartValue(getScaleX())
                            .setSpring(new SpringForce(Misc.hoverAnimationScaleOnUnHover)
                                    .setDampingRatio(Misc.hoverAnimationDampingRatio)
                                    .setStiffness(Misc.hoverAnimationStiffness));
                    
                    springAnimationY = new SpringAnimation(this, SpringAnimation.SCALE_Y)
                            .setStartValue(getScaleY())
                            .setSpring(new SpringForce(Misc.hoverAnimationScaleOnUnHover)
                                    .setDampingRatio(Misc.hoverAnimationDampingRatio)
                                    .setStiffness(Misc.hoverAnimationStiffness));
                    
                    springAnimationX.start();
                    springAnimationY.start();
                }
            }
        }
        
        return super.onGenericMotionEvent(event);
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        if (isClickable()) {
            setHighlightBackgroundColor();
        } else if (isSelected()) {
            setSelectedBackgroundColor();
        }
    }
    
    private void setSelectedBackgroundColor() {
        if (AccessibilityPreferences.INSTANCE.isHighlightMode()) {
            setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        } else {
            setBackgroundTintList(ColorStateList.valueOf(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getSelectedBackground()));
        }
    }
    
    private void setHighlightBackgroundColor() {
        if (AccessibilityPreferences.INSTANCE.isHighlightMode()) {
            LayoutBackground.setBackground(getContext(), this, null, Misc.roundedCornerFactor);
            setBackgroundTintList(ColorStateList.valueOf(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getHighlightBackground()));
        } else {
            setBackground(null);
            setBackground(Utils.getRippleDrawable(getBackground(), Misc.roundedCornerFactor));
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(@Nullable SharedPreferences sharedPreferences, @Nullable String key) {
        if (Objects.equals(key, AppearancePreferences.accentColor)) {
            setHighlightBackgroundColor();
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (springAnimationX != null) {
            springAnimationX.cancel();
            setScaleX(1.0f);
        }
        
        if (springAnimationY != null) {
            springAnimationY.cancel();
            setScaleY(1.0f);
        }
        
        clearAnimation();
    }
}