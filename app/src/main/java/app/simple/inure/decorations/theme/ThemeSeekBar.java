package app.simple.inure.decorations.theme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import app.simple.inure.R;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.preferences.BehaviourPreferences;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.ColorUtils;
import top.defaults.drawabletoolbox.DrawableBuilder;

// TODO - make a custom seekbar
public class ThemeSeekBar extends AppCompatSeekBar implements ThemeChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    
    private ObjectAnimator primaryProgressAnimator;
    private ObjectAnimator secondaryProgressAnimator;
    
    private final float shadowRadius = 0F;
    private final float dY = 0F;
    
    public ThemeSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ThemeSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (!isInEditMode()) {
            setThumb(AppearancePreferences.INSTANCE.getCornerRadius());
            setColors(AppearancePreferences.INSTANCE.getCornerRadius());
        }
    
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setMaxHeight(getResources().getDimensionPixelOffset(R.dimen.seekbar_max_height));
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            ThemeManager.INSTANCE.addListener(this);
        }
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        setThumb(AppearancePreferences.INSTANCE.getCornerRadius());
    }
    
    @Override
    protected void onDetachedFromWindow() {
        ThemeManager.INSTANCE.removeListener(this);
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        clearAnimation();
        super.onDetachedFromWindow();
    }
    
    private void setThumb(float cornerRadius) {
        if (!isInEditMode()) {
            setThumb(new DrawableBuilder()
                    .rectangle()
                    .cornerRadius((int) cornerRadius)
                    .width(getResources().getDimensionPixelOffset(R.dimen.seekbar_thumb_size))
                    .height(getResources().getDimensionPixelOffset(R.dimen.seekbar_thumb_size))
                    .ripple(false)
                    .strokeColor(AppearancePreferences.INSTANCE.getAccentColor())
                    .strokeWidth(getResources().getDimensionPixelOffset(R.dimen.seekbar_stroke_size))
                    .solidColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground())
                    .build());
        }
    }
    
    @SuppressWarnings ("unused")
    @Deprecated
    private Drawable createProgressDrawable() {
        float r = 20;
        ShapeDrawable shape = new ShapeDrawable();
        shape.setShape(new RoundRectShape(new float[] {r, r, r, r, r, r, r, r}, null, null));
        
        shape.getPaint().setStyle(Paint.Style.FILL);
        shape.getPaint().setColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getDividerBackground());
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(4);
        shape.getPaint().setStrokeCap(Paint.Cap.ROUND);
        shape.getPaint().setShadowLayer(shadowRadius, 0, dY, ColorUtils.INSTANCE.changeAlpha(AppearancePreferences.INSTANCE.getAccentColor(), 0.8F));
        shape.getPaint().setColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getDividerBackground());
        
        ShapeDrawable shapeD = new ShapeDrawable();
        shapeD.getPaint().setStyle(Paint.Style.FILL);
        shapeD.getPaint().setColor(AppearancePreferences.INSTANCE.getAccentColor());
        shapeD.setShape(new RoundRectShape(new float[] {r, r, r, r, r, r, r, r}, null, null));
        ClipDrawable progress = new ClipDrawable(shapeD, Gravity.START, ClipDrawable.HORIZONTAL);
        
        ShapeDrawable secondary = new ShapeDrawable();
        secondary.getPaint().setStyle(Paint.Style.FILL);
        secondary.getPaint().setColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getDividerBackground());
        secondary.getPaint().setStyle(Paint.Style.STROKE);
        secondary.getPaint().setStrokeWidth(4);
        secondary.getPaint().setStrokeCap(Paint.Cap.ROUND);
        secondary.getPaint().setShadowLayer(shadowRadius, 0, dY, ColorUtils.INSTANCE.changeAlpha(AppearancePreferences.INSTANCE.getAccentColor(), 0.8F));
        secondary.getPaint().setColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getDividerBackground());
        ClipDrawable secondaryProgress = new ClipDrawable(secondary, Gravity.START, ClipDrawable.HORIZONTAL);
        
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[] {progress, shape, secondaryProgress});
        
        layerDrawable.setId(1, android.R.id.background);
        layerDrawable.setId(0, android.R.id.progress);
        layerDrawable.setId(2, android.R.id.secondaryProgress);
        
        return layerDrawable;
    }
    
    /**
     * We'll create our progress drawable here
     */
    public void setColors(float cornerRadius) {
        /*
         * fgGradDirection and/or bgGradDirection could be parameters
         * if you require other gradient directions eg LEFT_RIGHT.
         */
        GradientDrawable.Orientation fgGradDirection = GradientDrawable.Orientation.TOP_BOTTOM;
        GradientDrawable.Orientation bgGradDirection = GradientDrawable.Orientation.TOP_BOTTOM;
        
        //Background
        int divideFactor = 4;
        float radius = cornerRadius / divideFactor;
        ShapeDrawable backgroundShape = new ShapeDrawable();
        backgroundShape.setShape(new RoundRectShape(new float[] {radius, radius, radius, radius, radius, radius, radius, radius}, null, null));
        backgroundShape.getPaint().setStyle(Paint.Style.STROKE);
        backgroundShape.getPaint().setStrokeWidth(4);
        backgroundShape.getPaint().setStrokeCap(Paint.Cap.ROUND);
        backgroundShape.getPaint().setColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getDividerBackground());
        
        if (!isInEditMode()) {
            if (BehaviourPreferences.INSTANCE.isColoredShadow()) {
                backgroundShape.getPaint().setShadowLayer(shadowRadius, 0, dY, ColorUtils.INSTANCE.changeAlpha(AppearancePreferences.INSTANCE.getAccentColor(), 0.85F));
            } else {
                backgroundShape.getPaint().setShadowLayer(shadowRadius, 0, dY, ColorUtils.INSTANCE.changeAlpha(Color.GRAY, 0.8F));
            }
        }
        
        /*
         * This code block isn't being due to its limited customization
         * abilities, however it's left here for revision and reference
         * purposes here.
         */
        ClipDrawable backgroundClip = getClipDrawable(cornerRadius, bgGradDirection, divideFactor);
        backgroundClip.setLevel(10000);
        
        //SecondaryProgress
        GradientDrawable fg2GradDrawable = new GradientDrawable(fgGradDirection, new int[] {
                ColorUtils.INSTANCE.changeAlpha(AppearancePreferences.INSTANCE.getAccentColor(), 0.37F),
                ColorUtils.INSTANCE.changeAlpha(AppearancePreferences.INSTANCE.getAccentColor(), 0.37F)});
        fg2GradDrawable.setShape(GradientDrawable.RECTANGLE);
        fg2GradDrawable.setCornerRadius(cornerRadius / divideFactor);
        ClipDrawable fg2clip = new ClipDrawable(fg2GradDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
        
        //Progress
        GradientDrawable fg1GradDrawable = new GradientDrawable(fgGradDirection, new int[] {
                AppearancePreferences.INSTANCE.getAccentColor(),
                AppearancePreferences.INSTANCE.getAccentColor()});
        fg1GradDrawable.setShape(GradientDrawable.RECTANGLE);
        fg1GradDrawable.setCornerRadius(cornerRadius / divideFactor);
        LayerDrawable progressLayerDrawable = getLayerDrawable(fg1GradDrawable, backgroundShape, fg2clip);
        
        //Copy the existing ProgressDrawable bounds to the new one.
        Rect bounds = getProgressDrawable().getBounds();
        setProgressDrawable(progressLayerDrawable);
        getProgressDrawable().setBounds(bounds);
        
        //now force a redraw
        invalidate();
    }
    
    @NonNull
    private ClipDrawable getClipDrawable(float cornerRadius, GradientDrawable.Orientation bgGradDirection, int divideFactor) {
        GradientDrawable bgGradDrawable = new GradientDrawable(bgGradDirection, new int[] {
                ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getHighlightBackground(),
                ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getHighlightBackground()});
        bgGradDrawable.setShape(GradientDrawable.RECTANGLE);
        if (!isInEditMode()) {
            bgGradDrawable.setCornerRadius(cornerRadius / divideFactor);
        }
        return new ClipDrawable(bgGradDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
    }
    
    @NonNull
    private static LayerDrawable getLayerDrawable(GradientDrawable fg1GradDrawable, ShapeDrawable backgroundShape, ClipDrawable fg2clip) {
        ClipDrawable fg1clip = new ClipDrawable(fg1GradDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
        
        //Setup LayerDrawable and assign to progressBar
        Drawable[] progressDrawables = {backgroundShape, fg2clip, fg1clip};
        LayerDrawable progressLayerDrawable = new LayerDrawable(progressDrawables);
        progressLayerDrawable.setId(0, android.R.id.background);
        progressLayerDrawable.setId(1, android.R.id.secondaryProgress);
        progressLayerDrawable.setId(2, android.R.id.progress);
        return progressLayerDrawable;
    }
    
    @Override
    public synchronized void setMax(int max) {
        super.setMax(max);
        invalidate();
        requestLayout();
    }
    
    public void updateDrawable(float cornerRadius) {
        setThumb(cornerRadius);
        setColors(cornerRadius);
    }
    
    public void updateProgress(int value) {
        clearProgressAnimation();
        
        primaryProgressAnimator = ObjectAnimator.ofInt(this, "progress", getProgress(), value);
        primaryProgressAnimator.setDuration(1000L);
        primaryProgressAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        primaryProgressAnimator.setAutoCancel(true);
        primaryProgressAnimator.start();
    }
    
    public void updateProgress(int value, int max) {
        clearProgressAnimation();
        
        primaryProgressAnimator = ObjectAnimator.ofInt(this, "progress", getProgress(), value);
        primaryProgressAnimator.setDuration(950L);
        primaryProgressAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        primaryProgressAnimator.setAutoCancel(true);
        primaryProgressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (getProgress() == 0) {
                    setMax(max);
                }
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (getMax() != max) {
                    setMax(max);
                }
            }
            
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                if (getMax() != max) {
                    setMax(max);
                }
            }
        });
        
        primaryProgressAnimator.start();
    }
    
    public void updateSecondaryProgress(int value) {
        clearSecondaryProgressAnimation();
        secondaryProgressAnimator = ObjectAnimator.ofInt(this, "secondaryProgress", getSecondaryProgress(), value);
        secondaryProgressAnimator.setDuration(1000L);
        secondaryProgressAnimator.setInterpolator(Utils.getInterpolator());
        secondaryProgressAnimator.setAutoCancel(true);
        secondaryProgressAnimator.start();
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Objects.equals(key, AppearancePreferences.accentColor)) {
            setColors(AppearancePreferences.INSTANCE.getCornerRadius());
            setThumb(AppearancePreferences.INSTANCE.getCornerRadius());
        }
    }
    
    private void clearProgressAnimation() {
        if (primaryProgressAnimator != null) {
            primaryProgressAnimator.removeAllListeners();
            primaryProgressAnimator.cancel();
        }
    }
    
    private void clearSecondaryProgressAnimation() {
        if (secondaryProgressAnimator != null) {
            secondaryProgressAnimator.removeAllListeners();
            secondaryProgressAnimator.cancel();
        }
    }
    
    @Override
    public void clearAnimation() {
        super.clearAnimation();
        clearProgressAnimation();
        clearSecondaryProgressAnimation();
    }
}
