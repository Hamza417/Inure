package app.simple.inure.decorations.theme;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;

import com.google.android.material.card.MaterialCardView;

import androidx.annotation.NonNull;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;

public class ThemeMaterialCardView extends MaterialCardView implements ThemeChangedListener {
    
    private ValueAnimator valueAnimator;
    
    public ThemeMaterialCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ThemeMaterialCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setCardBackgroundColor(Color.WHITE);
        setBackground(false);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThemeManager.INSTANCE.addListener(this);
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        setBackground(animate);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThemeManager.INSTANCE.removeListener(this);
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }
    
    private void setBackground(boolean animate) {
        if (animate) {
            valueAnimator = Utils.animateBackgroundColor(this,
                    ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground());
        } else {
            setBackgroundTintList(ColorStateList.valueOf(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground()));
        }
    }
}
