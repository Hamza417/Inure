package app.simple.inure.decorations.ripple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import app.simple.inure.util.ColorUtils;
import app.simple.inure.util.ViewUtils;

/**
 * {@link androidx.appcompat.widget.AppCompatTextView} but with animated
 * background
 */
public class DynamicRippleTextView extends TypeFaceTextView {
    
    private int highlightColor = -1;
    
    public DynamicRippleTextView(@NonNull Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
    }
    
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
            case MotionEvent.ACTION_DOWN -> {
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
            case MotionEvent.ACTION_MOVE,
                 MotionEvent.ACTION_CANCEL,
                 MotionEvent.ACTION_UP -> {
                if (AccessibilityPreferences.INSTANCE.isHighlightMode() && isClickable()) {
                    animate()
                            .scaleY(1F)
                            .scaleX(1F)
                            .alpha(1F)
                            .setInterpolator(new LinearOutSlowInInterpolator())
                            .setDuration(getResources().getInteger(R.integer.animation_duration))
                            .start();
                }
                
                return super.onTouchEvent(event);
            }
        }
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        ViewUtils.INSTANCE.triggerHover(this, event);
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
            setBackgroundTintList(ColorStateList.valueOf(
                    ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getSelectedBackground()));
        }
    }
    
    private void setHighlightBackgroundColor() {
        if (AccessibilityPreferences.INSTANCE.isHighlightMode()) {
            if (highlightColor == -1) {
                LayoutBackground.setBackground(getContext(), this, null, Misc.roundedCornerFactor, highlightColor);
                setBackgroundTintList(ColorStateList.valueOf(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getHighlightBackground()));
            } else {
                LayoutBackground.setBackground(getContext(), this, null, Misc.roundedCornerFactor, highlightColor);
                setBackgroundTintList(ColorStateList.valueOf(ColorUtils.INSTANCE.changeAlpha(highlightColor, Misc.highlightColorAlpha)));
            }
        } else {
            setBackground(null);
            if (DevelopmentPreferences.INSTANCE.get(DevelopmentPreferences.PADDING_LESS_POPUP_MENUS)) {
                setBackground(Utils.getRippleDrawable(getBackground()));
            } else {
                setBackground(Utils.getRippleDrawable(getBackground(), Misc.roundedCornerFactor));
            }
        }
    }
    
    public void setRippleColor(@NonNull ColorStateList colorStateList) {
        if (getBackground() instanceof android.graphics.drawable.RippleDrawable) {
            ((android.graphics.drawable.RippleDrawable) getBackground()).setColor(colorStateList);
        } else {
            setBackground(Utils.getRippleDrawable(getBackground(), Misc.roundedCornerFactor));
            ((android.graphics.drawable.RippleDrawable) getBackground()).setColor(colorStateList);
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(@Nullable SharedPreferences sharedPreferences, @Nullable String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        try {
            switch (key) {
                case AppearancePreferences.ACCENT_COLOR,
                     AccessibilityPreferences.IS_HIGHLIGHT_STROKE,
                     AccessibilityPreferences.IS_HIGHLIGHT_MODE -> {
                    setHighlightBackgroundColor();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAnimation();
        setScaleX(1);
        setScaleY(1);
    }
    
    public void setHighlightColor(int highlightColor) {
        this.highlightColor = highlightColor;
    }
}
