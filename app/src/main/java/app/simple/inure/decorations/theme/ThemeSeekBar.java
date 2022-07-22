package app.simple.inure.decorations.theme;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import app.simple.inure.R;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.ColorUtils;
import top.defaults.drawabletoolbox.DrawableBuilder;

// TODO - make a custom seekbar
public class ThemeSeekBar extends AppCompatSeekBar implements ThemeChangedListener {
    
    private ObjectAnimator objectAnimator;
    
    public ThemeSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ThemeSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setThumb();
        setDrawables();
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThemeManager.INSTANCE.addListener(this);
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        setThumb();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        ThemeManager.INSTANCE.removeListener(this);
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
    
        super.onDetachedFromWindow();
    }
    
    private void setThumb() {
        setThumb(new DrawableBuilder()
                .oval()
                .width(getResources().getDimensionPixelOffset(R.dimen.seekbar_thumb_size))
                .height(getResources().getDimensionPixelOffset(R.dimen.seekbar_thumb_size))
                .ripple(false)
                .strokeColor(AppearancePreferences.INSTANCE.getAccentColor())
                .strokeWidth(getResources().getDimensionPixelOffset(R.dimen.seekbar_stroke_size))
                .solidColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground())
                .build());
    
        invalidate();
    }
    
    private void setDrawables() {
        setThumb();
        setProgressDrawable(createProgressDrawable());
    }
    
    private Drawable createProgressDrawable() {
        float r = 20;
        ShapeDrawable shape = new ShapeDrawable();
        shape.setShape(new RoundRectShape(new float[] {r, r, r, r, r, r, r, r}, null, null));
        
        shape.getPaint().setStyle(Paint.Style.FILL);
        shape.getPaint().setColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getDividerBackground());
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(4);
        shape.getPaint().setStrokeCap(Paint.Cap.ROUND);
        shape.getPaint().setShadowLayer(50F, 0, 5, ColorUtils.INSTANCE.changeAlpha(AppearancePreferences.INSTANCE.getAccentColor(), 216));
        shape.getPaint().setColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getDividerBackground());
        
        ShapeDrawable shapeD = new ShapeDrawable();
        shapeD.getPaint().setStyle(Paint.Style.FILL);
        shapeD.getPaint().setColor(AppearancePreferences.INSTANCE.getAccentColor());
        shapeD.setShape(new RoundRectShape(new float[] {r, r, r, r, r, r, r, r}, null, null));
        ClipDrawable clipDrawable = new ClipDrawable(shapeD, Gravity.START, ClipDrawable.HORIZONTAL);
        
        return new LayerDrawable(new Drawable[] {clipDrawable, shape});
    }
    
    private Drawable createThumbDrawable() {
        float r = 200;
        ShapeDrawable shape = new ShapeDrawable();
        shape.setShape(new RoundRectShape(new float[] {r, r, r, r, r, r, r, r}, null, null));
        
        shape.getPaint().setStyle(Paint.Style.FILL);
        shape.getPaint().setColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getDividerBackground());
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(4);
        shape.getPaint().setStrokeCap(Paint.Cap.ROUND);
        shape.getPaint().setShadowLayer(50F, 0, 5, ColorUtils.INSTANCE.changeAlpha(AppearancePreferences.INSTANCE.getAccentColor(), 216));
        shape.getPaint().setColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getDividerBackground());
        
        ShapeDrawable shapeD = new ShapeDrawable();
        shapeD.getPaint().setStyle(Paint.Style.FILL);
        shapeD.getPaint().setColor(AppearancePreferences.INSTANCE.getAccentColor());
        shapeD.setShape(new RoundRectShape(new float[] {r, r, r, r, r, r, r, r}, null, null));
        ClipDrawable clipDrawable = new ClipDrawable(shapeD, Gravity.START, ClipDrawable.HORIZONTAL);
        
        return new LayerDrawable(new Drawable[] {clipDrawable, shape});
    }
    
    @Override
    public synchronized void setMax(int max) {
        super.setMax(max);
        invalidate();
        requestLayout();
    }
    
    public void updateSeekbar(int value) {
        objectAnimator = ObjectAnimator.ofInt(this, "progress", getProgress(), value);
        objectAnimator.setDuration(1000L);
        objectAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        objectAnimator.setAutoCancel(true);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                invalidate();
                requestLayout();
            }
            
            @Override
            public void onAnimationCancel(Animator animation) {
            
            }
            
            @Override
            public void onAnimationRepeat(Animator animation) {
            
            }
        });
        objectAnimator.start();
    }
}
