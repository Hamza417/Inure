package app.simple.inure.decorations.ripple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import java.util.Objects;

import androidx.annotation.Nullable;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.util.ViewUtils;

public class DynamicRippleLinearLayout extends LinearLayout implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    public DynamicRippleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        setBackgroundColor(Color.TRANSPARENT);
        setBackground(null);
        setBackground(Utils.getRippleDrawable(getBackground()));
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
        if (Objects.equals(key, AppearancePreferences.accentColor)) {
            init();
        }
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
}