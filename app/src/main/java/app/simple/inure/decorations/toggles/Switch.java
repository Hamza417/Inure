package app.simple.inure.decorations.toggles;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import app.simple.inure.R;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Accent;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.ViewUtils;

/**
 * @noinspection FieldCanBeLocal
 */
public class Switch extends View implements SharedPreferences.OnSharedPreferenceChangeListener, ThemeChangedListener {
    
    private final Paint thumbPaint = new Paint();
    private final Paint backgroundPaint = new Paint();
    private final Paint elevationPaint = new Paint();
    private final RectF backgroundRect = new RectF();
    
    private Drawable thumbDrawable;
    
    private ValueAnimator thumbXAnimator;
    private ValueAnimator thumbYAnimator;
    private ValueAnimator thumbSizeAnimator;
    private ValueAnimator backgroundAnimator;
    private ValueAnimator elevationAnimator;
    private ValueAnimator elevationColorAnimator;
    
    private OnCheckedChangeListener onCheckedChangeListener;
    
    private float height = 0;
    private float thumbDiameter = 0;
    private int backgroundColor = 0;
    
    // X and Y coordinates of the thumb
    private float thumbX = 0;
    private float thumbY = 0;
    
    // Width and height of the switch
    private float width = 0;
    
    // Padding and diameter of the thumb
    private float thumbPadding = 0;
    private float currentThumbScale = 1;
    
    // Constants
    private final float CORNER_RADIUS = 200;
    private final float TENSION = 3.5F;
    private final float SHADOW_SCALE_RGB = 0.85F;
    private final float SHADOW_SCALE_ALPHA = 0.4F;
    private final float FIXED_THUMB_SCALE = 1F;
    private final float THUMB_SCALE_ON_TOUCH = 1.50F;
    private final float SHADOW_Y_OFFSET = 10F;
    private final float MINIMUM_SHADOW_RADIUS = 5F;
    
    /**
     * Radius of the shadow around the background
     */
    private float shadowRadius = MINIMUM_SHADOW_RADIUS;
    private int elevationColor = Color.TRANSPARENT;
    private int elevation = 0;
    
    /**
     * Duration of the animations
     */
    private int duration = 500; // Subject to change according to the global animation duration
    
    private boolean isChecked = false;
    
    public Switch(Context context) {
        super(context);
        init();
    }
    
    public Switch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public Switch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setClipToOutline(false);
        
        if (AppearancePreferences.INSTANCE.getColoredIconShadows()) {
            shadowRadius = 10F;
        } else {
            shadowRadius = 0F;
        }
        
        ColorMatrix matrix = new ColorMatrix();
        matrix.setScale(SHADOW_SCALE_RGB, SHADOW_SCALE_RGB, SHADOW_SCALE_RGB, SHADOW_SCALE_ALPHA);
        elevationPaint.setAntiAlias(true);
        elevationPaint.setColorFilter(new ColorMatrixColorFilter(matrix));
        
        backgroundPaint.setAntiAlias(true);
        
        thumbPaint.setAntiAlias(true);
        thumbPaint.setColor(Color.WHITE);
        thumbPaint.setStyle(Paint.Style.FILL);
        // thumbPaint.setShadowLayer(shadowRadius, 0, 0, Color.WHITE);
        
        backgroundColor = ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor();
        duration = getResources().getInteger(R.integer.animation_duration);
        elevation = getResources().getDimensionPixelSize(R.dimen.app_views_elevation) * 2;
        
        thumbDrawable = ContextCompat.getDrawable(getContext(), R.drawable.switch_thumb);
        thumbDrawable.setTint(Color.WHITE);
        
        post(() -> {
            width = getWidth();
            height = getHeight();
            
            thumbDiameter = height - thumbPadding;
            
            backgroundRect.set(0, 0, width, height);
            
            setOnClickListener(v -> {
                isChecked = !isChecked;
                animateThumbX();
                animateBackgroundColor();
                animateElevation();
                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(isChecked);
                }
            });
            
            try { // I like cheating :)
                ((ViewGroup) getParent()).setClipToOutline(false);
                ((ViewGroup) getParent()).setClipChildren(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            updateSwitchState();
            setElevation(elevation);
            ViewUtils.INSTANCE.addShadow(this);
        });
    }
    
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // Draw shadow
        elevationPaint.setColor(backgroundColor);
        elevationPaint.setShadowLayer(shadowRadius, 0, SHADOW_Y_OFFSET, elevationColor);
        canvas.drawRoundRect(backgroundRect, CORNER_RADIUS, CORNER_RADIUS, elevationPaint);
        
        // Draw background
        backgroundPaint.setColor(backgroundColor);
        // backgroundPaint.setShadowLayer(shadowRadius, 0, 10, elevationColor);
        canvas.drawRoundRect(backgroundRect, CORNER_RADIUS, CORNER_RADIUS, backgroundPaint);
        
        // Draw thumb
        canvas.drawCircle(thumbX, thumbY, (thumbDiameter / 2) * currentThumbScale, thumbPaint);
        // Log.d("Switch", "thumbX: " + thumbX + " thumbY: " + thumbY + " thumbDiameter: " + thumbDiameter);
        
        // Position thumb based on currentThumbPosition
        // canvas.translate(thumbX, thumbY);
        
        super.onDraw(canvas);
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                getParent().requestDisallowInterceptTouchEvent(true);
                animateThumbSize(true);
                return super.onTouchEvent(event);
            }
            case MotionEvent.ACTION_MOVE -> {
                thumbX = event.getX();
                thumbY = event.getY();
                /*
                 * Move the switch thumb on x axis but keep it within the bounds of the switch
                 * If the bounds is exceeded, set the thumb position to the bounds.
                 */
                invalidate();
                return super.onTouchEvent(event);
            }
            case MotionEvent.ACTION_CANCEL -> {
                Log.d("Switch", "ACTION_CANCEL");
            }
            case MotionEvent.ACTION_UP -> {
                getParent().requestDisallowInterceptTouchEvent(false);
                animateThumbSize(false);
                animateThumbX();
                animateThumbY();
                return super.onTouchEvent(event);
            }
        }
        
        return super.onTouchEvent(event);
    }
    
    private void updateSwitchState() {
        if (isChecked) {
            thumbX = width - thumbDiameter / 2 - thumbPadding / 2;
            currentThumbScale = FIXED_THUMB_SCALE;
            backgroundColor = AppearancePreferences.INSTANCE.getAccentColor();
            elevationColor = AppearancePreferences.INSTANCE.getAccentColor();
            shadowRadius = elevation;
        } else {
            thumbX = thumbDiameter / 2 + thumbPadding / 2;
            currentThumbScale = FIXED_THUMB_SCALE;
            backgroundColor = ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor();
            elevationColor = Color.TRANSPARENT;
            shadowRadius = MINIMUM_SHADOW_RADIUS;
        }
        
        invalidate();
    }
    
    private void animateThumbX() {
        if (thumbXAnimator != null && thumbXAnimator.isRunning()) {
            thumbXAnimator.cancel();
        }
        
        if (isChecked) {
            thumbXAnimator = ValueAnimator.ofFloat(thumbX, width - thumbDiameter / 2 - thumbPadding / 2);
            thumbXAnimator.setInterpolator(new OvershootInterpolator(TENSION));
            thumbXAnimator.setDuration(duration);
            thumbXAnimator.addUpdateListener(animation -> {
                thumbX = (float) animation.getAnimatedValue();
                invalidate();
            });
        } else {
            thumbXAnimator = ValueAnimator.ofFloat(thumbX, thumbDiameter / 2 + thumbPadding / 2);
            thumbXAnimator.setInterpolator(new OvershootInterpolator(TENSION));
            thumbXAnimator.setDuration(duration);
            thumbXAnimator.addUpdateListener(animation -> {
                thumbX = (float) animation.getAnimatedValue();
                invalidate();
            });
        }
        
        thumbXAnimator.start();
    }
    
    private void animateThumbY() {
        if (thumbYAnimator != null && thumbYAnimator.isRunning()) {
            thumbYAnimator.cancel();
        }
        
        thumbYAnimator = ValueAnimator.ofFloat(thumbY, height / 2);
        thumbYAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        thumbYAnimator.setDuration(duration);
        thumbYAnimator.addUpdateListener(animation -> {
            thumbY = (float) animation.getAnimatedValue();
            invalidate();
        });
        
        thumbYAnimator.start();
    }
    
    private void animateThumbSize(boolean isTouchDown) {
        if (thumbSizeAnimator != null && thumbSizeAnimator.isRunning()) {
            thumbSizeAnimator.cancel();
        }
        
        if (isTouchDown) {
            thumbSizeAnimator = ValueAnimator.ofFloat(currentThumbScale, THUMB_SCALE_ON_TOUCH);
            thumbSizeAnimator.setDuration(duration);
            thumbSizeAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
            thumbSizeAnimator.addUpdateListener(animation -> {
                currentThumbScale = (float) animation.getAnimatedValue();
                invalidate();
            });
        } else {
            thumbSizeAnimator = ValueAnimator.ofFloat(currentThumbScale, FIXED_THUMB_SCALE);
            thumbSizeAnimator.setDuration(duration);
            thumbSizeAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
            thumbSizeAnimator.addUpdateListener(animation -> {
                currentThumbScale = (float) animation.getAnimatedValue();
                invalidate();
            });
        }
        
        thumbSizeAnimator.start();
    }
    
    private void animateBackgroundColor() {
        if (backgroundAnimator != null && backgroundAnimator.isRunning()) {
            backgroundAnimator.cancel();
        }
        
        if (isChecked) {
            backgroundAnimator = ValueAnimator.ofArgb(backgroundColor, AppearancePreferences.INSTANCE.getAccentColor());
            backgroundAnimator.setDuration(duration);
            backgroundAnimator.addUpdateListener(animation -> {
                backgroundColor = (int) animation.getAnimatedValue();
                backgroundPaint.setColor(backgroundColor);
                invalidate();
            });
        } else {
            backgroundAnimator = ValueAnimator.ofArgb(backgroundColor, ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor());
            backgroundAnimator.setDuration(duration);
            backgroundAnimator.addUpdateListener(animation -> {
                backgroundColor = (int) animation.getAnimatedValue();
                backgroundPaint.setColor(backgroundColor);
                invalidate();
            });
        }
        
        backgroundAnimator.start();
    }
    
    private void animateElevation() {
        if (elevationAnimator != null && elevationAnimator.isRunning()) {
            elevationAnimator.cancel();
        }
        
        if (elevationColorAnimator != null && elevationColorAnimator.isRunning()) {
            elevationColorAnimator.cancel();
        }
        
        if (isChecked) {
            elevationAnimator = ValueAnimator.ofFloat(shadowRadius, elevation);
            elevationAnimator.setDuration(duration);
            elevationAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
            elevationAnimator.addUpdateListener(animation -> {
                shadowRadius = (float) animation.getAnimatedValue();
                invalidate();
            });
            
            elevationColorAnimator = ValueAnimator.ofArgb(elevationColor, AppearancePreferences.INSTANCE.getAccentColor());
            elevationColorAnimator.setDuration(duration);
            elevationColorAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
            elevationColorAnimator.addUpdateListener(animation -> {
                elevationColor = (int) animation.getAnimatedValue();
                invalidate();
            });
        } else {
            elevationAnimator = ValueAnimator.ofFloat(shadowRadius, MINIMUM_SHADOW_RADIUS);
            elevationAnimator.setDuration(duration);
            elevationAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
            elevationAnimator.addUpdateListener(animation -> {
                shadowRadius = (float) animation.getAnimatedValue();
                invalidate();
            });
            
            elevationColorAnimator = ValueAnimator.ofArgb(elevationColor, Color.TRANSPARENT);
            elevationColorAnimator.setDuration(duration);
            elevationColorAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
            elevationColorAnimator.addUpdateListener(animation -> {
                elevationColor = (int) animation.getAnimatedValue();
                invalidate();
            });
        }
        
        elevationAnimator.start();
        elevationColorAnimator.start();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = getResources().getDimensionPixelSize(R.dimen.switch_width);
        int desiredHeight = getResources().getDimensionPixelSize(R.dimen.switch_height);
        
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int width;
        int height;
        
        // Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            // Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            // Be whatever you want
            width = desiredWidth;
        }
        
        // Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            // Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            // Be whatever you want
            height = desiredHeight;
        }
        
        /*
         * Set the dimensions for the switch thumb
         */
        thumbPadding = (float) getResources().getDimensionPixelSize(R.dimen.switch_thumb_dimensions) / 2;
        thumbDiameter = getResources().getDimensionPixelSize(R.dimen.switch_thumb_dimensions);
        thumbY = (float) height / 2;
        thumbX = thumbDiameter / 2 + thumbPadding / 2;
        
        // MUST CALL THIS
        setMeasuredDimension(width, height);
        updateSwitchState();
        
        // Update switch state
        invalidate();
    }
    
    private void animateEverything() {
        animateThumbX();
        animateBackgroundColor();
        animateElevation();
    }
    
    public void setChecked(boolean checked, boolean animate) {
        isChecked = checked;
        if (animate) {
            animateEverything();
        } else {
            updateSwitchState();
        }
    }
    
    public boolean isChecked() {
        return isChecked;
    }
    
    public void setChecked(boolean checked) {
        isChecked = checked;
        updateSwitchState();
    }
    
    public void toggle() {
        isChecked = !isChecked;
        animateEverything();
    }
    
    public void setOnSwitchCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
    
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        ThemeChangedListener.super.onThemeChanged(theme, animate);
        if (!isChecked) {
            if (animate) {
                animateEverything();
            } else {
                updateSwitchState();
            }
        }
    }
    
    @Override
    public void onAccentChanged(@NonNull Accent accent) {
        ThemeChangedListener.super.onAccentChanged(accent);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThemeManager.INSTANCE.addListener(this);
        app.simple.inure.preferences.SharedPreferences.INSTANCE.registerSharedPreferencesListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThemeManager.INSTANCE.removeListener(this);
        app.simple.inure.preferences.SharedPreferences.INSTANCE.unregisterSharedPreferenceChangeListener(this);
    }
}
