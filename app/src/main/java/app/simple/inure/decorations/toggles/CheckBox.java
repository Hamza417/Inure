package app.simple.inure.decorations.toggles;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import app.simple.inure.R;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.preferences.BehaviourPreferences;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Accent;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;

public class CheckBox extends View implements ThemeChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    
    private final Paint background = new Paint();
    private final Paint elevationPaint = new Paint();
    private final Paint check = new Paint();
    
    private final RectF backgroundRect = new RectF();
    
    private Drawable checkedIcon;
    
    private ValueAnimator animator = null;
    private ValueAnimator colorAnimator = null;
    private ValueAnimator elevationAnimator = null;
    
    private OnCheckedChangeListener listener;
    
    private int backgroundColor;
    private int elevationColor;
    
    private boolean isChecked = false;
    
    private float x;
    private float y;
    private float checkIconRatio = 0.8f;
    private int duration = 200;
    private float cornerRadius = 10;
    private float shadowRadius = 10F;
    
    public CheckBox(Context context) {
        super(context);
        init();
    }
    
    public CheckBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    public CheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    
    private void init() {
        setClipToOutline(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setClickable(true);
        setFocusable(true);
        setContentDescription("Checkbox");
        
        background.setAntiAlias(true);
        background.setStyle(Paint.Style.FILL);
        
        check.setAntiAlias(true);
        check.setStyle(Paint.Style.FILL);
        
        checkedIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_check);
        checkedIcon.setTint(Color.WHITE);
        
        if (!isInEditMode()) {
            cornerRadius = AppearancePreferences.INSTANCE.getCornerRadius() / 4F;
        } else {
            cornerRadius = 10;
        }
        
        backgroundColor = ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor();
        duration = getResources().getInteger(R.integer.animation_duration);
        
        setLayoutParams(new LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.checkbox_dimensions),
                getResources().getDimensionPixelSize(R.dimen.checkbox_dimensions)));
        
        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.checkbox_dimensions));
        setMinimumWidth(getResources().getDimensionPixelSize(R.dimen.checkbox_dimensions));
        
        if (!isInEditMode()) {
            if (AppearancePreferences.INSTANCE.getColoredIconShadows()) {
                shadowRadius = 10F;
            } else {
                shadowRadius = 0F;
            }
        } else {
            shadowRadius = 10F;
        }
        
        if (!isInEditMode()) {
            if (BehaviourPreferences.INSTANCE.isColoredShadow()) {
                elevationColor = AppearancePreferences.INSTANCE.getAccentColor();
            } else {
                elevationColor = Color.DKGRAY;
            }
        } else {
            elevationColor = Color.DKGRAY;
        }
        
        post(() -> {
            x = getWidth() / 2f;
            y = getHeight() / 2f;
            updateChecked(); // Update everything post layout to avoid missing graphics issues
            
            try { // I like cheating :)
                ((ViewGroup) getParent()).setClipToOutline(false);
                ((ViewGroup) getParent()).setClipChildren(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        setOnClickListener(v -> toggle(true));
        
        updateChecked();
    }
    
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // Draw the shadow
        // elevationPaint.setColor(elevationColor);
        // elevationPaint.setShadowLayer(shadowRadius, 0, 0, elevationColor);
        
        // Draw the background based on checked state
        background.setColor(backgroundColor);
        backgroundRect.set(0, 0, getWidth(), getHeight());
        background.setShadowLayer(shadowRadius, 0, 0, elevationColor);
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, background);
        
        checkedIcon.draw(canvas);
        
        super.onDraw(canvas);
    }
    
    private void animateFinalState() {
        clearAnimation();
        
        if (isChecked) {
            animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(duration);
            animator.setInterpolator(new OvershootInterpolator());
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                checkedIcon.setAlpha((int) (255 * value));
                checkedIcon.setBounds((int) (x - (x * checkIconRatio * value)),
                        (int) (y - (y * checkIconRatio * value)),
                        (int) (x + (x * checkIconRatio * value)),
                        (int) (y + (y * checkIconRatio * value)));
                invalidate();
            });
            
            colorAnimator = ValueAnimator.ofArgb(ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor(),
                    AppearancePreferences.INSTANCE.getAccentColor());
            colorAnimator.setDuration(duration);
            colorAnimator.setInterpolator(new DecelerateInterpolator());
            colorAnimator.addUpdateListener(animation -> {
                backgroundColor = (int) animation.getAnimatedValue();
                invalidate();
            });
            
            int endColor;
            
            if (BehaviourPreferences.INSTANCE.isColoredShadow()) {
                endColor = AppearancePreferences.INSTANCE.getAccentColor();
            } else {
                endColor = Color.DKGRAY;
            }
            
            elevationAnimator = ValueAnimator.ofArgb(elevationColor, endColor);
            elevationAnimator.setDuration(duration);
            elevationAnimator.setInterpolator(new DecelerateInterpolator());
            elevationAnimator.addUpdateListener(animation -> {
                elevationColor = (int) animation.getAnimatedValue();
                invalidate();
            });
        } else {
            animator = ValueAnimator.ofFloat(1, 0);
            animator.setDuration(duration);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                // checkRect.set(x - (x * value), y - (y * value), x + (x * value), y + (y * value));
                checkedIcon.setAlpha((int) (255 * value));
                checkedIcon.setBounds((int) (x - (x * checkIconRatio * value)),
                        (int) (y - (y * checkIconRatio * value)),
                        (int) (x + (x * checkIconRatio * value)),
                        (int) (y + (y * checkIconRatio * value)));
                
                invalidate();
            });
            
            colorAnimator = ValueAnimator.ofArgb(AppearancePreferences.INSTANCE.getAccentColor(),
                    ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor());
            colorAnimator.setDuration(duration);
            colorAnimator.setInterpolator(new AccelerateInterpolator());
            colorAnimator.addUpdateListener(animation -> {
                backgroundColor = (int) animation.getAnimatedValue();
                invalidate();
            });
            
            elevationAnimator = ValueAnimator.ofArgb(elevationColor, Color.TRANSPARENT);
            elevationAnimator.setDuration(duration);
            elevationAnimator.setInterpolator(new AccelerateInterpolator());
            elevationAnimator.addUpdateListener(animation -> {
                elevationColor = (int) animation.getAnimatedValue();
                invalidate();
            });
        }
        
        animator.start();
        colorAnimator.start();
        elevationAnimator.start();
    }
    
    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.CheckBox");
        info.setCheckable(true);
        info.setChecked(isChecked());
        CharSequence desc = getContentDescription();
        if (desc == null || desc.length() == 0) {
            desc = isChecked() ? "On" : "Off";
        }
        info.setContentDescription(desc);
    }
    
    @Override
    public boolean performClick() {
        boolean result = super.performClick();
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
        return result;
    }
    
    private void updateChecked() {
        clearAnimation();
        
        if (isChecked) {
            backgroundColor = AppearancePreferences.INSTANCE.getAccentColor();
            elevationColor = AppearancePreferences.INSTANCE.getAccentColor();
            // shadowRadius = 10F;
            checkedIcon.setAlpha(255);
            checkedIcon.setBounds(
                    (int) (x - (x * checkIconRatio)),
                    (int) (y - (y * checkIconRatio * 1)),
                    (int) (x + (x * checkIconRatio * 1)),
                    (int) (y + (y * checkIconRatio * 1)));
        } else {
            backgroundColor = ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor();
            elevationColor = Color.TRANSPARENT;
            // shadowRadius = 0F;
            checkedIcon.setAlpha(0);
            checkedIcon.setBounds(
                    (int) (x - (x * checkIconRatio)),
                    (int) (y - (y * checkIconRatio * 0F)),
                    (int) (x + (x * checkIconRatio * 0F)),
                    (int) (y + (y * checkIconRatio * 0F)));
            
            // ^ You could just set it to 0 or 1, where's fun in that?
        }
        
        invalidate();
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public boolean isChecked() {
        return isChecked;
    }
    
    public void setChecked(boolean checked) {
        isChecked = checked;
        updateChecked();
        
        // This method shouldn't notify the listener
    }
    
    public void setChecked(boolean checked, boolean animate) {
        isChecked = checked;
        if (animate) {
            animateFinalState();
        } else {
            updateChecked();
        }
    }
    
    public void toggle() {
        isChecked = !isChecked;
        
        if (listener != null) {
            listener.onCheckedChanged(isChecked);
        }
        
        animateFinalState();
    }
    
    public void toggle(boolean animate) {
        isChecked = !isChecked;
        
        if (listener != null) {
            listener.onCheckedChanged(isChecked);
        }
        
        if (animate) {
            animateFinalState();
        } else {
            updateChecked();
        }
    }
    
    public void animateToggle() {
        isChecked = !isChecked;
        animateFinalState();
        
        if (listener != null) {
            listener.onCheckedChanged(isChecked);
        }
    }
    
    public void check() {
        isChecked = true;
        animateFinalState();
        
        if (listener != null) {
            listener.onCheckedChanged(isChecked);
        }
    }
    
    public void check(boolean animate) {
        isChecked = true;
        
        if (listener != null) {
            listener.onCheckedChanged(isChecked);
        }
        
        if (animate) {
            animateFinalState();
        } else {
            updateChecked();
        }
    }
    
    public void uncheck() {
        isChecked = false;
        animateFinalState();
        
        if (listener != null) {
            listener.onCheckedChanged(isChecked);
        }
    }
    
    public void uncheck(boolean animate) {
        isChecked = false;
        
        if (listener != null) {
            listener.onCheckedChanged(isChecked);
        }
        
        if (animate) {
            animateFinalState();
        } else {
            updateChecked();
        }
    }
    
    public float getCheckIconRatio() {
        return checkIconRatio;
    }
    
    public void setCheckIconRatio(float ratio) {
        checkIconRatio = ratio;
        invalidate();
    }
    
    public float getCornerRadius() {
        return cornerRadius;
    }
    
    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        invalidate();
    }
    
    public Drawable getCheckedIcon() {
        return checkedIcon;
    }
    
    public void setCheckedIcon(Drawable drawable) {
        checkedIcon = drawable;
        invalidate();
    }
    
    public void setCheckedIconColor(int color) {
        checkedIcon.setTint(color);
        invalidate();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = getResources().getDimensionPixelSize(R.dimen.checkbox_dimensions);
        int desiredHeight = getResources().getDimensionPixelSize(R.dimen.checkbox_dimensions);
        
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int width;
        int height;
        
        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }
        
        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }
        
        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            app.simple.inure.preferences.SharedPreferences.INSTANCE.registerSharedPreferencesListener(this);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.unregisterSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void clearAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }
        
        if (elevationAnimator != null) {
            elevationAnimator.cancel();
        }
        
        super.clearAnimation();
    }
    
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        switch (key) {
            case AppearancePreferences.ACCENT_COLOR, AppearancePreferences.THEME, BehaviourPreferences.COLORED_SHADOWS -> {
                animateFinalState();
            }
        }
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        ThemeChangedListener.super.onThemeChanged(theme, animate);
        animateFinalState();
    }
    
    @Override
    public void onAccentChanged(@NonNull Accent accent) {
        ThemeChangedListener.super.onAccentChanged(accent);
        animateFinalState();
    }
}
