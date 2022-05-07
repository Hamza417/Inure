package app.simple.inure.decorations.theme;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import app.simple.inure.R;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.ColorUtils;

public class ThemeIcon extends AppCompatImageView implements ThemeChangedListener {
    
    private ValueAnimator valueAnimator;
    private int tintMode;
    
    public ThemeIcon(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    
    public ThemeIcon(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ThemeIcon, 0, 0);
        tintMode = typedArray.getInteger(R.styleable.ThemeIcon_tintType, 0);
        setTintColor(tintMode, false);
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
    
    private void setTintColor(int tintMode, boolean animate) {
        switch (tintMode) {
            case 0: {
                setTint(ThemeManager.INSTANCE.getTheme().getIconTheme().getRegularIconColor(), animate);
                break;
            }
            case 1: {
                setTint(ThemeManager.INSTANCE.getTheme().getIconTheme().getSecondaryIconColor(), animate);
                break;
            }
            case 2: {
                setTint(ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent), animate);
                break;
            }
            case 3: {
                // custom tint
            }
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThemeManager.INSTANCE.addListener(this);
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        setTintColor(tintMode, animate);
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