package app.simple.inure.decorations.tablayout.indicators;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import app.simple.inure.decorations.tablayout.InureTabLayout;

/**
 * Created by Andy671
 */

public class LineFadeIndicator implements AnimatedIndicatorInterface, ValueAnimator.AnimatorUpdateListener {
    private Paint paint;
    private RectF rectF;
    
    private int height;
    private int edgeRadius;
    
    private ValueAnimator valueAnimator;
    
    private InureTabLayout inureTabLayout;
    
    private int startXLeft, startXRight, endXLeft, endXRight;
    
    private int originColor, startColor, endColor;
    
    public LineFadeIndicator(InureTabLayout inureTabLayout) {
        this.inureTabLayout = inureTabLayout;
        
        valueAnimator = new ValueAnimator();
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(DEFAULT_DURATION);
        valueAnimator.addUpdateListener(this);
        valueAnimator.setIntValues(0, 255);
        
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        
        rectF = new RectF();
        
        startXLeft = (int) inureTabLayout.getChildXLeft(inureTabLayout.getCurrentPosition());
        startXRight = (int) inureTabLayout.getChildXRight(inureTabLayout.getCurrentPosition());
        
        edgeRadius = -1;
    }
    
    public void setEdgeRadius(int edgeRadius) {
        this.edgeRadius = edgeRadius;
        
        inureTabLayout.invalidate();
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
        
        if (edgeRadius == -1) {
            edgeRadius = height;
        }
    }
    
    @Override
    public void setIntValues(int startXLeft, int endXLeft,
            int startXCenter, int endXCenter,
            int startXRight, int endXRight) {
        this.startXLeft = startXLeft;
        this.startXRight = startXRight;
        this.endXLeft = endXLeft;
        this.endXRight = endXRight;
    }
    
    @Override
    public void setCurrentPlayTime(long currentPlayTime) {
        valueAnimator.setCurrentPlayTime(currentPlayTime);
    }
    
    @Override
    public void draw(Canvas canvas) {
        rectF.left = startXLeft + height / 2;
        rectF.right = startXRight - height / 2;
        rectF.top = inureTabLayout.getHeight() - height;
        rectF.bottom = inureTabLayout.getHeight();
        
        paint.setColor(startColor);
        canvas.drawRoundRect(rectF, edgeRadius, edgeRadius, paint);
        
        rectF.left = endXLeft + height / 2;
        rectF.right = endXRight - height / 2;
        
        paint.setColor(endColor);
        canvas.drawRoundRect(rectF, edgeRadius, edgeRadius, paint);
    }
    
    @Override
    public long getDuration() {
        return valueAnimator.getDuration();
    }
}
