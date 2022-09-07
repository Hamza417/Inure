package app.simple.inure.decorations.tablayout.indicators;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import app.simple.inure.decorations.tablayout.InureTabLayout;

/**
 * Created by Andy671
 */

public class PointFadeIndicator implements AnimatedIndicatorInterface, ValueAnimator.AnimatorUpdateListener {
    private Paint paint;
    
    private int height;
    
    private ValueAnimator valueAnimator;
    
    private InureTabLayout inureTabLayout;
    
    private int startX, endX;
    
    private int originColor, startColor, endColor;
    
    public PointFadeIndicator(InureTabLayout inureTabLayout) {
        this.inureTabLayout = inureTabLayout;
        
        valueAnimator = new ValueAnimator();
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(DEFAULT_DURATION);
        valueAnimator.addUpdateListener(this);
        valueAnimator.setIntValues(0, 255);
        
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        
        startX = (int) inureTabLayout.getChildXCenter(inureTabLayout.getCurrentPosition());
    }
    
    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        int startAlpha = 255 - (int) valueAnimator.getAnimatedValue();
        startColor = Color.argb(startAlpha, Color.red(originColor), Color.green(originColor), Color.blue(originColor));
        
        int endAlpha = (int) valueAnimator.getAnimatedValue();
        endColor = Color.argb(endAlpha, Color.red(originColor), Color.green(originColor), Color.blue(originColor));
        
        inureTabLayout.invalidate();
    }
    
    @Override
    public void setSelectedTabIndicatorColor(@ColorInt int color) {
        this.originColor = color;
        startColor = color;
        endColor = Color.TRANSPARENT;
    }
    
    @Override
    public void setSelectedTabIndicatorHeight(int height) {
        this.height = height;
    }
    
    @Override
    public void setIntValues(int startXLeft, int endXLeft,
            int startXCenter, int endXCenter,
            int startXRight, int endXRight) {
        startX = startXCenter;
        endX = endXCenter;
    }
    
    @Override
    public void setCurrentPlayTime(long currentPlayTime) {
        valueAnimator.setCurrentPlayTime(currentPlayTime);
    }
    
    @Override
    public void draw(Canvas canvas) {
        paint.setColor(startColor);
        canvas.drawCircle(startX, canvas.getHeight() - height / 2, height / 2, paint);
        
        paint.setColor(endColor);
        canvas.drawCircle(endX, canvas.getHeight() - height / 2, height / 2, paint);
    }
    
    @Override
    public long getDuration() {
        return valueAnimator.getDuration();
    }
}
