package app.simple.inure.decorations.fastscroll;

import android.view.View;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public class DefaultAnimationHelper implements FastScroller.AnimationHelper {
    
    private static final int SHOW_DURATION_MILLIS = 250;
    private static final int HIDE_DURATION_MILLIS = 250;
    private static final Interpolator SHOW_SCROLLBAR_INTERPOLATOR =
            new LinearOutSlowInInterpolator();
    private static final Interpolator HIDE_SCROLLBAR_INTERPOLATOR =
            new FastOutLinearInInterpolator();
    private static final int AUTO_HIDE_SCROLLBAR_DELAY_MILLIS = 500;
    
    @NonNull
    private final View mView;
    
    private boolean mScrollbarAutoHideEnabled = true;
    
    private boolean mShowingScrollbar = true;
    private boolean mShowingPopup;
    
    public DefaultAnimationHelper(@NonNull View view) {
        mView = view;
    }
    
    @Override
    public void showScrollbar(@NonNull View trackView, @NonNull View thumbView) {
        
        if (mShowingScrollbar) {
            return;
        }
        mShowingScrollbar = true;
        
        trackView.animate()
                .alpha(1)
                .translationX(0)
                .setDuration(SHOW_DURATION_MILLIS)
                .setInterpolator(SHOW_SCROLLBAR_INTERPOLATOR)
                .start();
        
        thumbView.animate()
                .alpha(1)
                .translationX(0)
                .setDuration(SHOW_DURATION_MILLIS)
                .setInterpolator(SHOW_SCROLLBAR_INTERPOLATOR)
                .start();
    }
    
    @Override
    public void hideScrollbar(@NonNull View trackView, @NonNull View thumbView) {
        
        if (!mShowingScrollbar) {
            return;
        }
        mShowingScrollbar = false;
        
        boolean isLayoutRtl = mView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        int width = Math.max(trackView.getWidth(), thumbView.getWidth());
        float translationX;
        if (isLayoutRtl) {
            translationX = trackView.getLeft() == 0 ? -width : 0;
        } else {
            translationX = trackView.getRight() == mView.getWidth() ? width : 0;
        }
    
        trackView.animate()
                .alpha(0)
                .translationX(translationX)
                .setDuration(HIDE_DURATION_MILLIS)
                .setInterpolator(HIDE_SCROLLBAR_INTERPOLATOR)
                .start();
    
        thumbView.animate()
                .alpha(0)
                .translationX(translationX)
                .setDuration(HIDE_DURATION_MILLIS)
                .setInterpolator(HIDE_SCROLLBAR_INTERPOLATOR)
                .start();
    }
    
    @Override
    public boolean isScrollbarAutoHideEnabled() {
        return mScrollbarAutoHideEnabled;
    }
    
    public void setScrollbarAutoHideEnabled(boolean enabled) {
        mScrollbarAutoHideEnabled = enabled;
    }
    
    @Override
    public int getScrollbarAutoHideDelayMillis() {
        return AUTO_HIDE_SCROLLBAR_DELAY_MILLIS;
    }
    
    @Override
    public void showPopup(@NonNull View popupView) {
        
        if (mShowingPopup) {
            return;
        }
        mShowingPopup = true;
        
        popupView.animate()
                .alpha(1)
                .setDuration(SHOW_DURATION_MILLIS)
                .start();
    }
    
    @Override
    public void hidePopup(@NonNull View popupView) {
        
        if (!mShowingPopup) {
            return;
        }
        mShowingPopup = false;
        
        popupView.animate()
                .alpha(0)
                .setDuration(HIDE_DURATION_MILLIS)
                .start();
    }
}
