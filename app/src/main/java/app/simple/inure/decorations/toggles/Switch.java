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
import app.simple.inure.preferences.BehaviourPreferences;
import app.simple.inure.preferences.DevelopmentPreferences;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Accent;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;

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
    private float OVERSHOOT_TENSION = 1F; // Lower is smoother, higher is more bouncy
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
    private boolean isDragEnabled = true;
    private boolean isDragging = false;
    private boolean shouldClick = true;
    
    private String tag = "Switch";
    
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
        setDragEnabled(false); // Disable the drag permanently ??TODO: Remove this
        
        if (!isInEditMode()) {
            if (AppearancePreferences.INSTANCE.getColoredIconShadows()) {
                shadowRadius = 10F;
            } else {
                shadowRadius = 0F;
            }
        } else {
            shadowRadius = 10F;
        }
        
        ColorMatrix matrix = new ColorMatrix();
        matrix.setScale(SHADOW_SCALE_RGB, SHADOW_SCALE_RGB, SHADOW_SCALE_RGB, SHADOW_SCALE_ALPHA);
        elevationPaint.setAntiAlias(true);
        elevationPaint.setColorFilter(new ColorMatrixColorFilter(matrix));
        
        backgroundPaint.setAntiAlias(true);
        
        thumbPaint.setAntiAlias(true);
        thumbPaint.setColor(Color.WHITE);
        thumbPaint.setStyle(Paint.Style.FILL);
        
        backgroundColor = ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor();
        duration = getResources().getInteger(R.integer.animation_duration);
        elevation = getResources().getDimensionPixelSize(R.dimen.app_views_elevation) * 2;
        
        thumbDrawable = ContextCompat.getDrawable(getContext(), R.drawable.switch_thumb);
        thumbDrawable.setTint(Color.WHITE);
        
        post(() -> {
            setOnClickListener(v -> {
                if (shouldClick) {
                    isChecked = !isChecked;
                    animateThumbX();
                    animateBackgroundColor();
                    animateElevation();
                    if (onCheckedChangeListener != null) {
                        onCheckedChangeListener.onCheckedChanged(isChecked);
                    }
                }
            });
            
            try { // I like cheating :)
                ((ViewGroup) getParent()).setClipToOutline(false);
                ((ViewGroup) getParent()).setClipChildren(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        // backgroundPaint.setShadowLayer(shadowRadius, 0, 10, elevationColor); // We'll draw the shadow separately above
        canvas.drawRoundRect(backgroundRect, CORNER_RADIUS, CORNER_RADIUS, backgroundPaint);
        
        // Draw thumb
        //noinspection StatementWithEmptyBody
        if (isDragging) {
            // thumbPaint.setShadowLayer(20F, 0, 0, AppearancePreferences.INSTANCE.getAccentColor());
        } else {
            // thumbPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        }
        
        canvas.drawCircle(thumbX, thumbY, (thumbDiameter / 2) * currentThumbScale, thumbPaint);
        
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
                shouldClick = true; // Reset the click flag
                OVERSHOOT_TENSION = 3.5F;
                
                return super.onTouchEvent(event);
            }
            case MotionEvent.ACTION_MOVE -> {
                // Cancel some animations
                if (isDragEnabled()) {
                    if (thumbXAnimator != null && thumbXAnimator.isRunning()) {
                        thumbXAnimator.cancel();
                    }
                    
                    if (thumbYAnimator != null && thumbYAnimator.isRunning()) {
                        thumbYAnimator.cancel();
                    }
                    
                    if (thumbSizeAnimator != null && thumbSizeAnimator.isRunning()) {
                        thumbSizeAnimator.cancel();
                    }
                    
                    if (DevelopmentPreferences.INSTANCE.get(DevelopmentPreferences.isSwitchFancyDraggable)) {
                        thumbX = event.getX();
                        thumbY = event.getY();
                    } else {
                        thumbX = event.getX();
                        
                        if (thumbX < thumbDiameter / 2 + thumbPadding / 2) {
                            thumbX = thumbDiameter / 2 + thumbPadding / 2;
                        } else if (thumbX > width - thumbDiameter / 2 - thumbPadding / 2) {
                            thumbX = width - thumbDiameter / 2 - thumbPadding / 2;
                        }
                    }
                    
                    // The thumb is dragged, prevent the click event
                    shouldClick = false;
                    isDragging = true;
                    OVERSHOOT_TENSION = 1F;
                    
                    Log.d("Switch", "ACTION_MOVE");
                    
                    invalidate();
                }
                
                return super.onTouchEvent(event);
            }
            case MotionEvent.ACTION_CANCEL -> {
                isDragging = false;
                Log.d("Switch", "ACTION_CANCEL");
                
                return super.onTouchEvent(event);
            }
            case MotionEvent.ACTION_UP -> {
                getParent().requestDisallowInterceptTouchEvent(false);
                animateThumbSize(false);
                
                if (!shouldClick) {
                    /*
                     * If the user has dragged the thumb more than half the width of the switch, then
                     * set the switch to checked state, else set it to unchecked state
                     */
                    isDragging = false;
                    isChecked = thumbX >= width / 2;
                    setChecked(isChecked, true);
                    onCheckedChangeListener.onCheckedChanged(isChecked);
                }
                
                return super.onTouchEvent(event);
            }
        }
        
        return super.onTouchEvent(event);
    }
    
    public void updateSwitchState() {
        if (isChecked) {
            thumbX = width - thumbDiameter / 2 - thumbPadding / 2;
            currentThumbScale = FIXED_THUMB_SCALE;
            backgroundColor = AppearancePreferences.INSTANCE.getAccentColor();
            if (BehaviourPreferences.INSTANCE.isColoredShadow()) {
                elevationColor = AppearancePreferences.INSTANCE.getAccentColor();
            } else {
                elevationColor = Color.DKGRAY;
            }
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
            thumbXAnimator.setInterpolator(new OvershootInterpolator(OVERSHOOT_TENSION));
            thumbXAnimator.setDuration(duration);
            thumbXAnimator.addUpdateListener(animation -> {
                thumbX = (float) animation.getAnimatedValue();
                invalidate();
            });
        } else {
            thumbXAnimator = ValueAnimator.ofFloat(thumbX, thumbDiameter / 2 + thumbPadding / 2);
            thumbXAnimator.setInterpolator(new OvershootInterpolator(OVERSHOOT_TENSION));
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
            
            int endColor;
            
            if (BehaviourPreferences.INSTANCE.isColoredShadow()) {
                endColor = AppearancePreferences.INSTANCE.getAccentColor();
            } else {
                endColor = Color.DKGRAY;
            }
            
            elevationColorAnimator = ValueAnimator.ofArgb(elevationColor, endColor);
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
         * Set the dimensions for the switch elements
         */
        this.width = width;
        this.height = height;
        backgroundRect.set(0, 0, width, height);
        thumbPadding = (float) getResources().getDimensionPixelSize(R.dimen.switch_thumb_dimensions) / 2;
        thumbDiameter = getResources().getDimensionPixelSize(R.dimen.switch_thumb_dimensions);
        thumbY = (float) height / 2;
        thumbX = width - thumbDiameter / 2 + thumbPadding / 2;
        
        // MUST CALL THIS
        setMeasuredDimension(width, height);
        updateSwitchState();
    }
    
    private void animateEverything() {
        clearAnimation();
        animateThumbX();
        animateThumbY();
        animateBackgroundColor();
        animateElevation();
        animateThumbSize(false);
    }
    
    /**
     * Set the switch state without animation
     * For animated check, use {@link #setChecked(boolean, boolean)}
     *
     * @noinspection unused
     */
    public void setChecked(boolean checked) {
        isChecked = checked;
        updateSwitchState();
    }
    
    /**
     * Set the switch state with or without animation
     * For unanimated check, use {@link #setChecked(boolean)}
     *
     * @noinspection unused
     */
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
    
    /**
     * Toggle the switch with animation
     */
    public void toggle() {
        isChecked = !isChecked;
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(isChecked);
        }
        animateEverything();
    }
    
    /**
     * Check the switch without animation
     * For animated check, use {@link #check(boolean)}
     */
    public void check() {
        isChecked = true;
        updateSwitchState();
    }
    
    /**
     * Uncheck the switch without animation
     * For animated uncheck, use {@link #uncheck(boolean)}
     *
     * @noinspection unused
     */
    public void uncheck() {
        isChecked = false;
        updateSwitchState();
    }
    
    /**
     * Check the switch with or without animation
     * For unanimated check, use {@link #check()}
     *
     * @param animate Whether to animate the check or not
     */
    public void check(boolean animate) {
        isChecked = true;
        if (animate) {
            animateEverything();
        } else {
            updateSwitchState();
        }
    }
    
    /**
     * Uncheck the switch with or without animation
     * For unanimated uncheck, use {@link #uncheck()}
     *
     * @param animate Whether to animate the uncheck or not
     * @noinspection unused
     */
    public void uncheck(boolean animate) {
        isChecked = false;
        if (animate) {
            animateEverything();
        } else {
            updateSwitchState();
        }
    }
    
    @Override
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public boolean isDragEnabled() {
        return isDragEnabled;
    }
    
    public void setDragEnabled(boolean dragEnabled) {
        isDragEnabled = dragEnabled;
    }
    
    /**
     * Make the switch visible
     */
    public void visible() {
        updateSwitchState();
        setVisibility(VISIBLE);
    }
    
    /**
     * Make the switch invisible
     */
    public void invisible() {
        updateSwitchState();
        setVisibility(INVISIBLE);
    }
    
    /**
     * Make the switch gone
     */
    public void gone() {
        updateSwitchState();
        setVisibility(GONE);
    }
    
    public void setOnSwitchCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (key) {
            case BehaviourPreferences.coloredShadows -> {
                animateElevation();
            }
        }
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
        if (!isInEditMode()) {
            app.simple.inure.preferences.SharedPreferences.INSTANCE.registerSharedPreferencesListener(this);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        clearAnimation();
        super.onDetachedFromWindow();
        ThemeManager.INSTANCE.removeListener(this);
        app.simple.inure.preferences.SharedPreferences.INSTANCE.unregisterSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void clearAnimation() {
        if (thumbXAnimator != null && thumbXAnimator.isRunning()) {
            thumbXAnimator.cancel();
        }
        
        if (thumbYAnimator != null && thumbYAnimator.isRunning()) {
            thumbYAnimator.cancel();
        }
        
        if (thumbSizeAnimator != null && thumbSizeAnimator.isRunning()) {
            thumbSizeAnimator.cancel();
        }
        
        if (backgroundAnimator != null && backgroundAnimator.isRunning()) {
            backgroundAnimator.cancel();
        }
        
        if (elevationAnimator != null && elevationAnimator.isRunning()) {
            elevationAnimator.cancel();
        }
        
        if (elevationColorAnimator != null && elevationColorAnimator.isRunning()) {
            elevationColorAnimator.cancel();
        }
        
        super.clearAnimation();
    }
}
