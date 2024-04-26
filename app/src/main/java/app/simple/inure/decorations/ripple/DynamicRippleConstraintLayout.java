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
import androidx.constraintlayout.widget.ConstraintLayout;
import app.simple.inure.R;
import app.simple.inure.constants.Misc;
import app.simple.inure.decorations.corners.LayoutBackground;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.util.ColorUtils;
import app.simple.inure.util.ViewUtils;

public class DynamicRippleConstraintLayout extends ConstraintLayout implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    public DynamicRippleConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public DynamicRippleConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (!isInEditMode()) {
            setBackground(Utils.getRippleDrawable(getBackground()));
            setBackgroundColor(Color.TRANSPARENT);
            setDefaultBackground(false);
        }
    }
    
    /**
     * Use this method to track selection in {@link androidx.recyclerview.widget.RecyclerView}.
     * This will change the background according to the accent color and will also keep
     * save the ripple effect.
     *
     * @param selected true for selected item
     */
    public void setDefaultBackground(boolean selected) {
        if (selected) {
            setBackgroundTintList(null);
            setBackgroundTintList(ColorStateList.valueOf(ColorUtils.INSTANCE.changeAlpha(
                    ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent), Misc.highlightColorAlpha)));
            
            LayoutBackground.setBackground(getContext(), this, null);
        } else {
            setBackground(null);
            setBackground(Utils.getRippleDrawable(getBackground()));
        }
    }
    
    public void setWarningBackground(int color) {
        setBackgroundTintList(null);
        setBackgroundTintList(ColorStateList.valueOf(
                ColorUtils.INSTANCE.changeAlpha(color, Misc.highlightColorAlpha)));
        
        LayoutBackground.setBackground(getContext(), this, null);
    }
    
    public void removeRipple() {
        setBackground(null);
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
    
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        ViewUtils.INSTANCE.triggerHover(this, event);
        return super.onGenericMotionEvent(event);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) {
            return;
        }
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        clearAnimation();
        setScaleX(1);
        setScaleY(1);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Objects.equals(key, AppearancePreferences.ACCENT_COLOR)) {
            init();
        }
    }
}
