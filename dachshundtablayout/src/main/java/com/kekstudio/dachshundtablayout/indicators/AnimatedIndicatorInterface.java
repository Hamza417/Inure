package com.kekstudio.dachshundtablayout.indicators;

import android.graphics.Canvas;

import androidx.annotation.ColorInt;

/**
 * The interface you should implement for creating custom AnimatedIndicators.
 * <p>
 * Created by Andy671
 */

public interface AnimatedIndicatorInterface {
    
    /**
     * Default duration of the animation. Pass it to getDuration().
     */
    long DEFAULT_DURATION = 500;
    
    /**
     * *Can be called from DachshundTabLayout. Sets color of the indicator.
     *
     * @param color - color of the tab indicator.
     */
    void setSelectedTabIndicatorColor(@ColorInt int color);
    
    /**
     * *Can be called from DachshundTabLayout. Sets height of the indicator.
     *
     * @param height - height of the tab indicator.
     */
    void setSelectedTabIndicatorHeight(int height);
    
    /**
     * Updates values when swiping of the ViewPager starts.
     *
     * @param startXLeft   X-coordinate of the current tab left edge
     * @param endXLeft     X-coordinate edge of the finish destination tab left edge
     * @param startXCenter X-coordinate of the current tab center
     * @param endXCenter   X-coordinate of the finish destination tab center
     * @param startXRight  X-coordinate of the current tab right edge
     * @param endXRight    X-coordinate edge of the finish destination tab right edge
     */
    void setIntValues(int startXLeft, int endXLeft,
            int startXCenter, int endXCenter,
            int startXRight, int endXRight);
    
    /**
     * Updates currentPlayTime when swiping of the ViewPager occurs.
     * (e.g. if we are on half of the way // currentPlayTime == getDuration() / 2 )
     * <p>
     * You can use it in your animators (don't forget to Override getDuration() method)
     *
     * @param currentPlayTime swiping position in playTime.
     */
    void setCurrentPlayTime(long currentPlayTime);
    
    /**
     * Make your drawing calls here.
     * Call invalidate() when you need to redraw.
     *
     * @param canvas DachshundTabLayout canvas.
     */
    void draw(Canvas canvas);
    
    /**
     * Override it, if you want to work with animators, to make setCurrentPlayTime method work.
     *
     * @return the duration of your animator. Currently supporting only DEFAULT_DURATION.
     */
    long getDuration();
}
