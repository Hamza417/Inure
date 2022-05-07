package app.simple.inure.decorations.theme;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import app.simple.inure.R;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.ColorUtils;

public class ThemeButton extends AppCompatImageButton implements ThemeChangedListener {
    
    private ValueAnimator valueAnimator;
    protected int tintMode;
    
    public ThemeButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    
    public ThemeButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ThemeButton, 0, 0);
        tintMode = typedArray.getInteger(R.styleable.ThemeButton_buttonTintType, 0);
        setTint(getTintColor(tintMode), false);
    }
    
    private void setTint(int endColor, boolean animate) {
        if (animate) {
            valueAnimator = ValueAnimator.ofArgb(getImageTintList().getDefaultColor(), endColor);
            valueAnimator.setDuration(getResources().getInteger(R.integer.theme_change_duration));
            valueAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
            valueAnimator.addUpdateListener(animation -> setImageTintList(ColorStateList.valueOf((int) animation.getAnimatedValue())));
            valueAnimator.start();
        } else {
            setImageTintList(ColorStateList.valueOf(endColor));
        }
    }
    
    private int getTintColor(int tintMode) {
        switch (tintMode) {
            case 0: {
                return ThemeManager.INSTANCE.getTheme().getIconTheme().getRegularIconColor();
            }
            case 1: {
                return ThemeManager.INSTANCE.getTheme().getIconTheme().getSecondaryIconColor();
            }
            case 2: {
                return ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent);
            }
            case 3: {
                return Color.WHITE;
            }
            case 4: {
                return Color.GRAY;
            }
        }
    
        return getImageTintList().getDefaultColor();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setTint(getTintColor(tintMode), false);
        } else {
            setTint(getTintColor(4), false);
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThemeManager.INSTANCE.addListener(this);
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        setTint(getTintColor(tintMode), animate);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThemeManager.INSTANCE.removeListener(this);
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }
}
