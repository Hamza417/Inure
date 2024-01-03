package app.simple.inure.decorations.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import app.simple.inure.R;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.themes.manager.ThemeManager;

public class CheckBox extends View {
    
    private final Paint background = new Paint();
    private final Paint backgroundShadow = new Paint();
    private final Paint check = new Paint();
    private final RectF backgroundRect = new RectF();
    private final RectF checkRect = new RectF();
    private Drawable checkedIcon;
    private ValueAnimator animator = null;
    
    private boolean isChecked = false;
    private float x;
    private float y;
    private int duration = 200;
    
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
        
        background.setAntiAlias(true);
        background.setStyle(Paint.Style.FILL);
        
        backgroundShadow.setAntiAlias(true);
        backgroundShadow.setStyle(Paint.Style.FILL);
        
        check.setAntiAlias(true);
        check.setStyle(Paint.Style.FILL);
        
        checkedIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_check);
        checkedIcon.setTint(Color.WHITE);
        
        setLayoutParams(new LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.checkbox_dimensions),
                getResources().getDimensionPixelSize(R.dimen.checkbox_dimensions)));
        
        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.checkbox_dimensions));
        setMinimumWidth(getResources().getDimensionPixelSize(R.dimen.checkbox_dimensions));
        
        post(() -> {
            x = getWidth() / 2f;
            y = getHeight() / 2f;
            Log.d("CheckBox", "x: " + x + " y: " + y);
        });
    }
    
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // Draw the background based on checked state
        if (isChecked) {
            background.setColor(AppearancePreferences.INSTANCE.getAccentColor());
            background.setShadowLayer(10, 0, 0, AppearancePreferences.INSTANCE.getAccentColor());
        } else {
            background.setColor(ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor());
            background.setShadowLayer(10, 0, 0, ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor());
        }
        
        backgroundRect.set(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(backgroundRect, 10, 10, background);
        
        // Draw the check icon if checked
        if (isChecked) {
            check.setColor(Color.WHITE);
            checkRect.set(x - (x * 0.5f), y - (y * 0.5f), x + (x * 0.5f), y + (y * 0.5f));
            canvas.drawRoundRect(checkRect, 10, 10, check);
            checkedIcon.setBounds((int) (x - (x * 0.5f)), (int) (y - (y * 0.5f)), (int) (x + (x * 0.5f)), (int) (y + (y * 0.5f)));
            checkedIcon.draw(canvas);
        } else {
            check.setColor(ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor());
            checkRect.set(x - (x * 0.5f), y - (y * 0.5f), x + (x * 0.5f), y + (y * 0.5f));
            canvas.drawRoundRect(checkRect, 10, 10, check);
        }
        
        super.onDraw(canvas);
    }
    
    public void setChecked(boolean checked, boolean animate) {
        isChecked = checked;
        if (animate) {
            animateChecked();
        } else {
            invalidate();
        }
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
        invalidate();
    }
    
    public void toggle() {
        isChecked = !isChecked;
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
    
    private void animateChecked() {
        if (isChecked) {
            animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(duration);
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                checkRect.set(x - (x * value), y - (y * value), x + (x * value), y + (y * value));
                invalidate();
            });
        } else {
            animator = ValueAnimator.ofFloat(1, 0);
            animator.setDuration(duration);
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                checkRect.set(x - (x * value), y - (y * value), x + (x * value), y + (y * value));
                invalidate();
            });
        }
        animator.start();
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
    public void clearAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        
        super.clearAnimation();
    }
    
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        setOnClickListener(v -> {
            isChecked = !isChecked;
            animateChecked();
            listener.onCheckedChanged(this, isChecked);
        });
    }
    
    public interface OnCheckedChangeListener {
        void onCheckedChanged(CheckBox checkBox, boolean isChecked);
    }
}
