package com.kekstudio.dachshundtablayout.indicators;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.kekstudio.dachshundtablayout.DachshundTabLayout;

import androidx.annotation.ColorInt;

/**
 * Created by Andy671
 */

public class DachshundIndicator implements AnimatedIndicatorInterface, ValueAnimator.AnimatorUpdateListener {
    
    private Paint paint;
    private RectF rectF;
    private Rect rect;
    
    private int height;
    
    private ValueAnimator valueAnimatorLeft, valueAnimatorRight;
    
    private DachshundTabLayout dachshundTabLayout;
    
    private AccelerateInterpolator accelerateInterpolator;
    private DecelerateInterpolator decelerateInterpolator;
    
    private int leftX, rightX;
    
    public DachshundIndicator(DachshundTabLayout dachshundTabLayout) {
        this.dachshundTabLayout = dachshundTabLayout;
        
        valueAnimatorLeft = new ValueAnimator();
        valueAnimatorLeft.setDuration(DEFAULT_DURATION);
        valueAnimatorLeft.addUpdateListener(this);
        
        valueAnimatorRight = new ValueAnimator();
        valueAnimatorRight.setDuration(DEFAULT_DURATION);
        valueAnimatorRight.addUpdateListener(this);
        
        accelerateInterpolator = new AccelerateInterpolator();
        decelerateInterpolator = new DecelerateInterpolator();
        
        rectF = new RectF();
        rect = new Rect();
        
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        
        leftX = (int) dachshundTabLayout.getChildXCenter(dachshundTabLayout.getCurrentPosition());
        rightX = leftX;
    }
    
    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        leftX = (int) valueAnimatorLeft.getAnimatedValue();
        rightX = (int) valueAnimatorRight.getAnimatedValue();
        
        rect.top = dachshundTabLayout.getHeight() - height;
        rect.left = leftX - height / 2;
        rect.right = rightX + height / 2;
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
        boolean toRight = endXCenter - startXCenter >= 0;
        
        if (toRight) {
            valueAnimatorLeft.setInterpolator(accelerateInterpolator);
            valueAnimatorRight.setInterpolator(decelerateInterpolator);
        } else {
            valueAnimatorLeft.setInterpolator(decelerateInterpolator);
            valueAnimatorRight.setInterpolator(accelerateInterpolator);
        }
        
        valueAnimatorLeft.setIntValues(startXCenter, endXCenter);
        valueAnimatorRight.setIntValues(startXCenter, endXCenter);
    }
    
    @Override
    public void setCurrentPlayTime(long currentPlayTime) {
        valueAnimatorLeft.setCurrentPlayTime(currentPlayTime);
        valueAnimatorRight.setCurrentPlayTime(currentPlayTime);
    }
    
    @Override
    public void draw(Canvas canvas) {
        rectF.top = dachshundTabLayout.getHeight() - height;
        rectF.left = leftX - height / 2;
        rectF.right = rightX + height / 2;
        rectF.bottom = dachshundTabLayout.getHeight();
        
        canvas.drawRoundRect(rectF, height, height, paint);
    }
    
    @Override
    public long getDuration() {
        return valueAnimatorLeft.getDuration();
    }
}
