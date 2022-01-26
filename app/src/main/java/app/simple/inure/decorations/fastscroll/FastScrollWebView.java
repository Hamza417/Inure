package app.simple.inure.decorations.fastscroll;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import app.simple.inure.decorations.views.CustomWebView;

@SuppressLint ("MissingSuperCall")
public class FastScrollWebView extends CustomWebView implements ViewHelperProvider {
    
    @NonNull
    private final ViewHelper mViewHelper = new ViewHelper();
    
    public FastScrollWebView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        setVerticalScrollBarEnabled(false);
        setScrollContainer(true);
    }
    
    @NonNull
    @Override
    public FastScroller.ViewHelper getViewHelper() {
        return mViewHelper;
    }
    
    @Override
    public void draw(@NonNull Canvas canvas) {
        mViewHelper.draw(canvas);
    }
    
    @Override
    protected void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
        mViewHelper.onScrollChanged(left, top, oldLeft, oldTop);
    }
    
    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        return mViewHelper.onInterceptTouchEvent(event);
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return mViewHelper.onTouchEvent(event);
    }
    
    private class ViewHelper extends SimpleViewHelper {
        
        @Override
        protected void superDraw(@NonNull Canvas canvas) {
            FastScrollWebView.super.draw(canvas);
        }
        
        @Override
        protected void superOnScrollChanged(int left, int top, int oldLeft, int oldTop) {
            FastScrollWebView.super.onScrollChanged(left, top, oldLeft, oldTop);
        }
        
        @Override
        protected boolean superOnInterceptTouchEvent(@NonNull MotionEvent event) {
            return FastScrollWebView.super.onInterceptTouchEvent(event);
        }
        
        @Override
        protected boolean superOnTouchEvent(@NonNull MotionEvent event) {
            return FastScrollWebView.super.onTouchEvent(event);
        }
        
        @Override
        protected int computeVerticalScrollRange() {
            return FastScrollWebView.this.computeVerticalScrollRange();
        }
        
        @Override
        protected int computeVerticalScrollOffset() {
            return FastScrollWebView.this.computeVerticalScrollOffset();
        }
        
        @Override
        protected int getScrollX() {
            return FastScrollWebView.this.getScrollX();
        }
        
        @Override
        protected void scrollTo(int x, int y) {
            FastScrollWebView.this.scrollTo(x, y);
        }
    }
}
