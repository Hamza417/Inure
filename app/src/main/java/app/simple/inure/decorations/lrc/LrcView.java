package app.simple.inure.decorations.lrc;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import app.simple.inure.R;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.util.TypeFace;

public class LrcView extends View {
    
    private static final String DEFAULT_CONTENT = "Empty";
    private final HashMap <String, StaticLayout> mLrcMap = new HashMap <>();
    private final HashMap <String, StaticLayout> mStaticLayoutHashMap = new HashMap <>();
    private List <Lrc> mLrcData;
    private TextPaint mTextPaint;
    private String mDefaultContent;
    private int mCurrentLine;
    private final Runnable hideIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            isShowTimeIndicator = false;
            invalidateView();
        }
    };
    private float mLastMotionX;
    private float offset;
    private int mScaledTouchSlop;
    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            isUserScroll = false;
            scrollToPosition(mCurrentLine);
        }
    };
    private VelocityTracker mVelocityTracker;
    private int mMaximumFlingVelocity;
    private int mMinimumFlingVelocity;
    private float mLrcTextSize;
    private float mLrcLineSpaceHeight;
    private float lastMotionY;
    private int mNormalColor;
    private int mCurrentPlayLineColor;
    private float mNoLrcTextSize;
    private int mNoLrcTextColor;
    private boolean isDragging;
    private boolean isUserScroll;
    private OverScroller overScroller;
    private boolean isAutoAdjustPosition = true;
    private Drawable mPlayDrawable;
    private boolean isShowTimeIndicator;
    private int touchDelay;
    private Rect mPlayRect;
    private Paint mIndicatorPaint;
    private float mIndicatorLineWidth;
    private float mIndicatorTextSize;
    private int mCurrentIndicateLineTextColor;
    private int mIndicatorLineColor;
    private float mIndicatorMargin;
    private float mIconLineGap;
    private float mIconWidth;
    private float mIconHeight;
    private boolean isEnableShowIndicator = true;
    private int mIndicatorTextColor;
    private int mIndicatorTouchDelay;
    private boolean isCurrentTextBold;
    private boolean isLrcIndicatorTextBold;
    private OnPlayIndicatorLineListener mOnPlayIndicatorLineListener;
    
    public LrcView(Context context) {
        this(context, null);
    }
    
    public LrcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public LrcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LrcView);
        mLrcTextSize = typedArray.getDimension(R.styleable.LrcView_lrcTextSize, sp2px(context, 15));
        mLrcLineSpaceHeight = typedArray.getDimension(R.styleable.LrcView_lrcLineSpaceSize, dp2px(context, 20));
        touchDelay = typedArray.getInt(R.styleable.LrcView_lrcTouchDelay, 3500);
        mIndicatorTouchDelay = typedArray.getInt(R.styleable.LrcView_indicatorTouchDelay, 2500);
        mNormalColor = typedArray.getColor(R.styleable.LrcView_lrcNormalTextColor, Color.LTGRAY);
        mCurrentPlayLineColor = typedArray.getColor(R.styleable.LrcView_lrcCurrentTextColor, Color.WHITE);
        mNoLrcTextSize = typedArray.getDimension(R.styleable.LrcView_noLrcTextSize, dp2px(context, 20));
        mNoLrcTextColor = typedArray.getColor(R.styleable.LrcView_noLrcTextColor, Color.BLACK);
        mIndicatorLineWidth = typedArray.getDimension(R.styleable.LrcView_indicatorLineHeight, dp2px(context, 0.5f));
        mIndicatorTextSize = typedArray.getDimension(R.styleable.LrcView_indicatorTextSize, sp2px(context, 13));
        if (!isInEditMode()) {
            mIndicatorTextColor = typedArray.getColor(R.styleable.LrcView_indicatorTextColor, AppearancePreferences.INSTANCE.getAccentColor());
            mCurrentIndicateLineTextColor = typedArray.getColor(R.styleable.LrcView_currentIndicateLrcColor, AppearancePreferences.INSTANCE.getAccentColor());
            mIndicatorLineColor = typedArray.getColor(R.styleable.LrcView_indicatorLineColor, AppearancePreferences.INSTANCE.getAccentColor());
        }
        mIndicatorMargin = typedArray.getDimension(R.styleable.LrcView_indicatorStartEndMargin, dp2px(context, 5));
        mIconLineGap = typedArray.getDimension(R.styleable.LrcView_iconLineGap, dp2px(context, 3));
        mIconWidth = typedArray.getDimension(R.styleable.LrcView_playIconWidth, dp2px(context, 20));
        mIconHeight = typedArray.getDimension(R.styleable.LrcView_playIconHeight, dp2px(context, 20));
        mPlayDrawable = typedArray.getDrawable(R.styleable.LrcView_playIcon);
        mPlayDrawable = mPlayDrawable == null ? ContextCompat.getDrawable(context, R.drawable.ic_play) : mPlayDrawable;
        isCurrentTextBold = typedArray.getBoolean(R.styleable.LrcView_isLrcCurrentTextBold, true);
        isLrcIndicatorTextBold = typedArray.getBoolean(R.styleable.LrcView_isLrcIndicatorTextBold, false);
        typedArray.recycle();
        
        setupConfigs(context);
    }
    
    private void setupConfigs(Context context) {
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        overScroller = new OverScroller(context, new DecelerateInterpolator());
        overScroller.setFriction(0.1f);
        //        ViewConfiguration.getScrollFriction();  默认摩擦力 0.015f
    
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mLrcTextSize);
        if (!isInEditMode()) {
            mTextPaint.setTypeface(TypeFace.INSTANCE.getRegularTypeFace(getContext()));
        }
        mDefaultContent = DEFAULT_CONTENT;
        
        mIndicatorPaint = new Paint();
        mIndicatorPaint.setAntiAlias(true);
        mIndicatorPaint.setStrokeWidth(mIndicatorLineWidth);
        mIndicatorPaint.setColor(mIndicatorLineColor);
        mPlayRect = new Rect();
        mIndicatorPaint.setTextSize(mIndicatorTextSize);
        if (!isInEditMode()) {
            mIndicatorPaint.setTypeface(TypeFace.INSTANCE.getBoldTypeFace(getContext()));
        }
        
        setBackgroundColor(ColorUtils.setAlphaComponent(Color.BLACK, 150));
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mPlayRect.left = (int) mIndicatorMargin;
            mPlayRect.top = (int) (getHeight() / 2 - mIconHeight / 2);
            mPlayRect.right = (int) (mPlayRect.left + mIconWidth);
            mPlayRect.bottom = (int) (mPlayRect.top + mIconHeight);
            mPlayDrawable.setBounds(mPlayRect);
        }
    }
    
    private int getLrcWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
    
    private int getLrcHeight() {
        return getHeight();
    }
    
    private boolean isLrcEmpty() {
        return mLrcData == null || getLrcCount() == 0;
    }
    
    private int getLrcCount() {
        return mLrcData.size();
    }
    
    public void setLrcData(List <Lrc> lrcData) {
        resetView(DEFAULT_CONTENT);
        mLrcData = lrcData;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isLrcEmpty()) {
            drawEmptyText(canvas);
            return;
        }
        int indicatePosition = getIndicatePosition();
        mTextPaint.setTextSize(mLrcTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        float y = getLrcHeight() / 2f;
        float x = getLrcWidth() / 2f + getPaddingLeft();
        for (int i = 0; i < getLrcCount(); i++) {
            if (i > 0) {
                y += (getTextHeight(i - 1) + getTextHeight(i)) / 2f + mLrcLineSpaceHeight;
            }
            if (mCurrentLine == i) {
                mTextPaint.setColor(mCurrentPlayLineColor);
                mTextPaint.setFakeBoldText(isCurrentTextBold);
            } else if (indicatePosition == i && isShowTimeIndicator) {
                mTextPaint.setFakeBoldText(isLrcIndicatorTextBold);
                mTextPaint.setColor(mCurrentIndicateLineTextColor);
            } else {
                mTextPaint.setFakeBoldText(false);
                mTextPaint.setColor(mNormalColor);
            }
            drawLrc(canvas, x, y, i);
        }
        
        if (isShowTimeIndicator) {
            mPlayDrawable.draw(canvas);
            long time = mLrcData.get(indicatePosition).getTime();
            float timeWidth = mIndicatorPaint.measureText(LrcHelper.formatTime(time));
            mIndicatorPaint.setColor(mIndicatorLineColor);
            canvas.drawLine(mPlayRect.right + mIconLineGap, getHeight() / 2f,
                    getWidth() - timeWidth * 1.3f, getHeight() / 2f, mIndicatorPaint);
            int baseX = (int) (getWidth() - timeWidth * 1.1f);
            float baseline = getHeight() / 2f - (mIndicatorPaint.descent() - mIndicatorPaint.ascent()) / 2 - mIndicatorPaint.ascent();
            mIndicatorPaint.setColor(mIndicatorTextColor);
            canvas.drawText(LrcHelper.formatTime(time), baseX, baseline, mIndicatorPaint);
        }
    }
    
    private void drawLrc(Canvas canvas, float x, float y, int i) {
        String text = mLrcData.get(i).getText();
        StaticLayout staticLayout = mLrcMap.get(text);
        if (staticLayout == null) {
            mTextPaint.setTextSize(mLrcTextSize);
            staticLayout = new StaticLayout(text, mTextPaint, getLrcWidth(),
                    Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
            mLrcMap.put(text, staticLayout);
        }
        canvas.save();
        canvas.translate(x, y - staticLayout.getHeight() / 2f - offset);
        staticLayout.draw(canvas);
        canvas.restore();
    }
    
    private void drawEmptyText(Canvas canvas) {
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mNoLrcTextColor);
        mTextPaint.setTextSize(mNoLrcTextSize);
        canvas.save();
        StaticLayout staticLayout = new StaticLayout(mDefaultContent, mTextPaint,
                getLrcWidth(), Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        canvas.translate(getLrcWidth() / 2f + getPaddingLeft(), getLrcHeight() / 2f);
        staticLayout.draw(canvas);
        canvas.restore();
    }
    
    public void updateTime(long time) {
        if (isLrcEmpty()) {
            return;
        }
        int linePosition = getUpdateTimeLinePosition(time);
        if (mCurrentLine != linePosition) {
            mCurrentLine = linePosition;
            if (isUserScroll) {
                invalidateView();
                return;
            }
            ViewCompat.postOnAnimation(LrcView.this, scrollRunnable);
        }
    }
    
    private int getUpdateTimeLinePosition(long time) {
        int linePos = 0;
        for (int i = 0; i < getLrcCount(); i++) {
            Lrc lrc = mLrcData.get(i);
            if (time >= lrc.getTime()) {
                if (i == getLrcCount() - 1) {
                    linePos = getLrcCount() - 1;
                } else if (time < mLrcData.get(i + 1).getTime()) {
                    linePos = i;
                    break;
                }
            }
        }
        return linePos;
    }
    
    private void scrollToPosition(int linePosition) {
        float scrollY = getItemOffsetY(linePosition);
        final ValueAnimator animator = ValueAnimator.ofFloat(offset, scrollY);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                offset = (float) animation.getAnimatedValue();
                invalidateView();
            }
        });
        animator.setDuration(300);
        animator.start();
    }
    
    public int getIndicatePosition() {
        int pos = 0;
        float min = Float.MAX_VALUE;
        //itemOffset 和 mOffset 最小即当前位置
        for (int i = 0; i < mLrcData.size(); i++) {
            float offsetY = getItemOffsetY(i);
            float abs = Math.abs(offsetY - offset);
            if (abs < min) {
                min = abs;
                pos = i;
            }
        }
        return pos;
    }
    
    private float getItemOffsetY(int linePosition) {
        float tempY = 0;
        for (int i = 1; i <= linePosition; i++) {
            tempY += (getTextHeight(i - 1) + getTextHeight(i)) / 2 + mLrcLineSpaceHeight;
        }
        return tempY;
    }
    
    private float getTextHeight(int linePosition) {
        String text = mLrcData.get(linePosition).getText();
        StaticLayout staticLayout = mStaticLayoutHashMap.get(text);
        if (staticLayout == null) {
            mTextPaint.setTextSize(mLrcTextSize);
            staticLayout = new StaticLayout(text, mTextPaint,
                    getLrcWidth(), Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
            mStaticLayoutHashMap.put(text, staticLayout);
        }
        return staticLayout.getHeight();
    }
    
    private boolean overScrolled() {
        return offset > getItemOffsetY(getLrcCount() - 1) || offset < 0;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLrcEmpty()) {
            return super.onTouchEvent(event);
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                removeCallbacks(scrollRunnable);
                removeCallbacks(hideIndicatorRunnable);
                if (!overScroller.isFinished()) {
                    overScroller.abortAnimation();
                }
                mLastMotionX = event.getX();
                lastMotionY = event.getY();
                isUserScroll = true;
                isDragging = false;
                break;
    
            case MotionEvent.ACTION_MOVE:
                float moveY = event.getY() - lastMotionY;
                if (Math.abs(moveY) > mScaledTouchSlop) {
                    isDragging = true;
                    isShowTimeIndicator = isEnableShowIndicator;
                }
                if (isDragging) {
    
                    //                    if (mOffset < 0) {
                    //                        mOffset = Math.max(mOffset, -getTextHeight(0) - mLrcLineSpaceHeight);
                    //                    }
                    float maxHeight = getItemOffsetY(getLrcCount() - 1);
                    //                    if (mOffset > maxHeight) {
                    //                        mOffset = Math.min(mOffset, maxHeight + getTextHeight(getLrcCount() - 1) + mLrcLineSpaceHeight);
                    //                    }
                    if (offset < 0 || offset > maxHeight) {
                        moveY /= 3.5f;
                    }
                    offset -= moveY;
                    lastMotionY = event.getY();
                    invalidateView();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isDragging && (!isShowTimeIndicator || !onClickPlayButton(event))) {
                    isShowTimeIndicator = false;
                    invalidateView();
                    performClick();
                }
                handleActionUp(event);
                break;
        }
        //        return isDragging || super.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    
    private void handleActionUp(MotionEvent event) {
        if (isEnableShowIndicator) {
            ViewCompat.postOnAnimationDelayed(LrcView.this, hideIndicatorRunnable, mIndicatorTouchDelay);
        }
        if (isShowTimeIndicator && mPlayRect != null && onClickPlayButton(event)) {
            isShowTimeIndicator = false;
            invalidateView();
            if (mOnPlayIndicatorLineListener != null) {
                mOnPlayIndicatorLineListener.onPlay(mLrcData.get(getIndicatePosition()).getTime(),
                        mLrcData.get(getIndicatePosition()).getText());
            }
        }
        if (overScrolled() && offset < 0) {
            scrollToPosition(0);
            if (isAutoAdjustPosition) {
                ViewCompat.postOnAnimationDelayed(LrcView.this, scrollRunnable, touchDelay);
            }
            return;
        }
    
        if (overScrolled() && offset > getItemOffsetY(getLrcCount() - 1)) {
            scrollToPosition(getLrcCount() - 1);
            if (isAutoAdjustPosition) {
                ViewCompat.postOnAnimationDelayed(LrcView.this, scrollRunnable, touchDelay);
            }
            return;
        }
        
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
        float yVelocity = mVelocityTracker.getYVelocity();
        float absYVelocity = Math.abs(yVelocity);
        if (absYVelocity > mMinimumFlingVelocity) {
            overScroller.fling(0, (int) offset, 0, (int) (-yVelocity), 0,
                    0, 0, (int) getItemOffsetY(getLrcCount() - 1),
                    0, (int) getTextHeight(0));
            invalidateView();
        }
        releaseVelocityTracker();
        if (isAutoAdjustPosition) {
            ViewCompat.postOnAnimationDelayed(LrcView.this, scrollRunnable, touchDelay);
        }
    }
    
    private boolean onClickPlayButton(MotionEvent event) {
        float left = mPlayRect.left;
        float right = mPlayRect.right;
        float top = mPlayRect.top;
        float bottom = mPlayRect.bottom;
        float x = event.getX();
        float y = event.getY();
        return mLastMotionX > left && mLastMotionX < right && lastMotionY > top
                && lastMotionY < bottom && x > left && x < right && y > top && y < bottom;
    }
    
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (overScroller.computeScrollOffset()) {
            offset = overScroller.getCurrY();
            invalidateView();
        }
    }
    
    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    
    public void resetView(String defaultContent) {
        if (mLrcData != null) {
            mLrcData.clear();
        }
        mLrcMap.clear();
        mStaticLayoutHashMap.clear();
        mCurrentLine = 0;
        offset = 0;
        isUserScroll = false;
        isDragging = false;
        mDefaultContent = defaultContent;
        removeCallbacks(scrollRunnable);
        invalidate();
    }
    
    @Override
    public boolean performClick() {
        return super.performClick();
    }
    
    public int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
    
    public int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }
    
    public void pause() {
        isAutoAdjustPosition = false;
        invalidateView();
    }
    
    public boolean isPaused() {
        return !isAutoAdjustPosition;
    }
    
    public void resume() {
        isAutoAdjustPosition = true;
        ViewCompat.postOnAnimationDelayed(LrcView.this, scrollRunnable, touchDelay);
        invalidateView();
    }
    
    
    /*------------------Config-------------------*/
    
    private void invalidateView() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }
    
    public void setOnPlayIndicatorLineListener(OnPlayIndicatorLineListener onPlayIndicatorLineListener) {
        mOnPlayIndicatorLineListener = onPlayIndicatorLineListener;
    }
    
    public void setEmptyContent(String defaultContent) {
        mDefaultContent = defaultContent;
        invalidateView();
    }
    
    public void setLrcTextSize(float lrcTextSize) {
        mLrcTextSize = lrcTextSize;
        invalidateView();
    }
    
    public void setLrcLineSpaceHeight(float lrcLineSpaceHeight) {
        mLrcLineSpaceHeight = lrcLineSpaceHeight;
        invalidateView();
    }
    
    public void setTouchDelay(int touchDelay) {
        this.touchDelay = touchDelay;
        invalidateView();
    }
    
    public void setNormalColor(@ColorInt int normalColor) {
        mNormalColor = normalColor;
        invalidateView();
    }
    
    public void setCurrentPlayLineColor(@ColorInt int currentPlayLineColor) {
        mCurrentPlayLineColor = currentPlayLineColor;
        invalidateView();
    }
    
    public void setNoLrcTextSize(float noLrcTextSize) {
        mNoLrcTextSize = noLrcTextSize;
        invalidateView();
    }
    
    public void setNoLrcTextColor(@ColorInt int noLrcTextColor) {
        mNoLrcTextColor = noLrcTextColor;
        invalidateView();
    }
    
    public void setIndicatorLineWidth(float indicatorLineWidth) {
        mIndicatorLineWidth = indicatorLineWidth;
        invalidateView();
    }
    
    public void setIndicatorTextSize(float indicatorTextSize) {
        //        mIndicatorTextSize = indicatorTextSize;
        mIndicatorPaint.setTextSize(indicatorTextSize);
        invalidateView();
    }
    
    public void setCurrentIndicateLineTextColor(int currentIndicateLineTextColor) {
        mCurrentIndicateLineTextColor = currentIndicateLineTextColor;
        invalidateView();
    }
    
    public void setIndicatorLineColor(int indicatorLineColor) {
        mIndicatorLineColor = indicatorLineColor;
        invalidateView();
    }
    
    public void setIndicatorMargin(float indicatorMargin) {
        mIndicatorMargin = indicatorMargin;
        invalidateView();
    }
    
    public void setIconLineGap(float iconLineGap) {
        mIconLineGap = iconLineGap;
        invalidateView();
    }
    
    public void setIconWidth(float iconWidth) {
        mIconWidth = iconWidth;
        invalidateView();
    }
    
    public void setIconHeight(float iconHeight) {
        mIconHeight = iconHeight;
        invalidateView();
    }
    
    public void setEnableShowIndicator(boolean enableShowIndicator) {
        isEnableShowIndicator = enableShowIndicator;
        invalidateView();
    }
    
    public Drawable getPlayDrawable() {
        return mPlayDrawable;
    }
    
    public void setPlayDrawable(Drawable playDrawable) {
        mPlayDrawable = playDrawable;
        mPlayDrawable.setBounds(mPlayRect);
        invalidateView();
    }
    
    public void setIndicatorTextColor(int indicatorTextColor) {
        mIndicatorTextColor = indicatorTextColor;
        invalidateView();
    }
    
    public void setLrcCurrentTextBold(boolean bold) {
        isCurrentTextBold = bold;
        invalidateView();
    }
    
    public void setLrcIndicatorTextBold(boolean bold) {
        isLrcIndicatorTextBold = bold;
        invalidateView();
    }
    
    public interface OnPlayIndicatorLineListener {
        void onPlay(long time, String content);
    }
}
