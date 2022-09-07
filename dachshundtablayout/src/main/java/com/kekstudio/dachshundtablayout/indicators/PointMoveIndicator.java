package com.kekstudio.dachshundtablayout.indicators;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.animation.LinearInterpolator;

import com.kekstudio.dachshundtablayout.DachshundTabLayout;

import androidx.annotation.ColorInt;

/**
 * Created by Andy671
 */

public class PointMoveIndicator implements AnimatedIndicatorInterface, ValueAnimator.AnimatorUpdateListener {
    
    private Paint paint;
    private Rect rect;
    
    private int height;
    
    private ValueAnimator valueAnimator;
    
    private DachshundTabLayout dachshundTabLayout;
    
    private int frameX;
    
    public PointMoveIndicator(DachshundTabLayout dachshundTabLayout) {
        this.dachshundTabLayout = dachshundTabLayout;
        
        valueAnimator = new ValueAnimator();
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(DEFAULT_DURATION);
        valueAnimator.addUpdateListener(this);
        
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        
        rect = new Rect();
        
        frameX = (int) dachshundTabLayout.getChildXCenter(dachshundTabLayout.getCurrentPosition());
    }
    
    public void setInterpolator(TimeInterpolator interpolator) {
        valueAnimator.setInterpolator(interpolator);
    }
    
    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        frameX = (int) valueAnimator.getAnimatedValue();
        
        rect.left = frameX - height / 2;
        rect.right = frameX + height / 2;
        rect.top = dachshundTabLayout.getHeight() - height;
        rect.bottom = dachshundTabLayout.getHeight();
        
        dachshundTabLayout.invalidate(rect);
    }
    
    @Override
    public void setSelectedTabIndicatorColor(@ColorInt int color) {
        paint.setColor(color);
    }
    
    @Override
    public void setSelectedTabIndicatorHeight(int height) {
        this.height = height;
    }
    
    @Override
    public void setIntValues(int startXLeft, int endXLeft,
            int startXCenter, int endXCenter,
            int startXRight, int endXRight) {
        valueAnimator.setIntValues(startXCenter, endXCenter);
    }
    
    @Override
    public void setCurrentPlayTime(long currentPlayTime) {
        valueAnimator.setCurrentPlayTime(currentPlayTime);
    }
    
    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(frameX, canvas.getHeight() - height / 2, height / 2, paint);
    }
    
    @Override
    public long getDuration() {
        return valueAnimator.getDuration();
    }
    
}
