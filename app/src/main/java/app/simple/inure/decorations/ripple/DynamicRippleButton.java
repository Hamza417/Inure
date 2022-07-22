package app.simple.inure.decorations.ripple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import app.simple.inure.R;
import app.simple.inure.decorations.corners.LayoutBackground;
import app.simple.inure.preferences.AccessibilityPreferences;
import app.simple.inure.themes.manager.ThemeManager;

@Deprecated ()
/*
 * Use @link{DynamicRippleTextView} since button is extension
 * of it, it works much better than button
 */
public class DynamicRippleButton extends androidx.appcompat.widget.AppCompatButton {
    public DynamicRippleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        
        if (AccessibilityPreferences.INSTANCE.isHighlightMode()) {
            LayoutBackground.setBackground(getContext(), this, attrs, 2F);
            setBackgroundTintList(ColorStateList.valueOf(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getHighlightBackground()));
        } else {
            setBackground(Utils.getRippleDrawable(getBackground(), 2F));
        }
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (AccessibilityPreferences.INSTANCE.isHighlightMode()) {
                    animate()
                            .scaleY(0.8F)
                            .scaleX(0.8F)
                            .alpha(0.5F)
                            .setInterpolator(new LinearOutSlowInInterpolator())
                            .setDuration(getResources().getInteger(R.integer.animation_duration))
                            .start();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (AccessibilityPreferences.INSTANCE.isHighlightMode()) {
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
}
