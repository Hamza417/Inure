package app.simple.inure.decorations.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import app.simple.inure.R;
import app.simple.inure.util.ColorUtils;

public class LoaderImageView extends AppCompatImageView {
    
    public LoaderImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    
    public LoaderImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.LoaderImageView, 0, 0);
        
        switch (typedArray.getInt(R.styleable.LoaderImageView_loaderStyle, 0)) {
            case 0: {
                setImageResource(R.drawable.ic_loader);
                startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.loader));
                break;
            }
            case 1: {
                setImageResource(R.drawable.ic_loader_still);
                setImageTintList(ColorStateList.valueOf(ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent)));
                break;
            }
        }
        
        setFocusable(false);
        setClickable(false);
    }
    
    public void loaded() {
        clearAnimation();
        animateColor(Color.parseColor("#27ae60"));
    }
    
    public void error() {
        clearAnimation();
        animateColor(Color.parseColor("#a93226"));
    }
    
    private void animateColor(int toColor) {
        ValueAnimator valueAnimator = ValueAnimator.ofArgb(getDefaultColor(), toColor);
        valueAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        valueAnimator.setDuration(getResources().getInteger(R.integer.animation_duration));
        valueAnimator.addUpdateListener(animation -> setImageTintList(ColorStateList.valueOf((int) animation.getAnimatedValue())));
        valueAnimator.start();
    }
    
    private int getDefaultColor() {
        try {
            return getImageTintList().getDefaultColor();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAnimation();
    }
}
