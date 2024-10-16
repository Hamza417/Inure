package app.simple.inure.decorations.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import app.simple.inure.R;
import app.simple.inure.util.ColorUtils;

public class LoaderImageView extends AppCompatImageView {
    
    private static final String TAG = LoaderImageView.class.getSimpleName();
    private boolean isLoaded = false;
    
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
        isLoaded = true;
        clearAnimation();
        animateColor(0xFF27AE60);
    }
    
    public void error() {
        isLoaded = true;
        clearAnimation();
        animateColor(0xFFA93226);
    }
    
    public void reset() {
        isLoaded = false;
        clearAnimation();
        setImageResource(R.drawable.ic_loader);
        startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.loader));
        // Clear tint
        setImageTintList(null);
        setVisibility(View.VISIBLE);
    }
    
    public void start() {
        if (!isLoaded) {
            startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.loader));
        } else {
            Log.d(TAG, "Already loaded, if you want to reset call reset()");
        }
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
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }
}
