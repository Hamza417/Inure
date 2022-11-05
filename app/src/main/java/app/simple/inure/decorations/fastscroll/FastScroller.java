package app.simple.inure.decorations.fastscroll;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.math.MathUtils;
import androidx.core.util.Consumer;
import app.simple.inure.R;

public class FastScroller {
    
    private final int mMinTouchTargetSize;
    private final int mTouchSlop;
    
    @NonNull
    private final ViewGroup mView;
    @NonNull
    private final ViewHelper mViewHelper;
    @NonNull
    private final AnimationHelper mAnimationHelper;
    private final int mTrackWidth;
    private final int mThumbWidth;
    private final int mThumbHeight;
    @NonNull
    private final View mTrackView;
    @NonNull
    private final View mThumbView;
    @NonNull
    private final TextView mPopupView;
    @NonNull
    private final Rect mTempRect = new Rect();
    @Nullable
    private Rect mUserPadding;
    private boolean mScrollbarEnabled;
    private int mThumbOffset;
    private float mDownX;
    private float mDownY;
    private float mLastY;
    private float mDragStartY;
    private int mDragStartThumbOffset;
    private boolean mDragging;
    @NonNull
    private final Runnable mAutoHideScrollbarRunnable = this :: autoHideScrollbar;
    
    public FastScroller(@NonNull ViewGroup view, @NonNull ViewHelper viewHelper,
            @Nullable Rect padding, @NonNull Drawable trackDrawable,
            @NonNull Drawable thumbDrawable, @NonNull Consumer <TextView> popupStyle,
            @NonNull AnimationHelper animationHelper) {
        
        mMinTouchTargetSize = view.getResources().getDimensionPixelSize(
                R.dimen.afs_min_touch_target_size);
        Context context = view.getContext();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        
        mView = view;
        mViewHelper = viewHelper;
        mUserPadding = padding;
        mAnimationHelper = animationHelper;
        
        mTrackWidth = trackDrawable.getIntrinsicWidth();
        mThumbWidth = thumbDrawable.getIntrinsicWidth();
        mThumbHeight = thumbDrawable.getIntrinsicHeight();
        
        mTrackView = new View(context);
        mTrackView.setBackground(trackDrawable);
        mThumbView = new View(context);
        mThumbView.setBackground(thumbDrawable);
        mPopupView = new AppCompatTextView(context);
        mPopupView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        popupStyle.accept(mPopupView);
        
        ViewGroupOverlay overlay = mView.getOverlay();
        overlay.add(mTrackView);
        overlay.add(mThumbView);
        overlay.add(mPopupView);
        
        postAutoHideScrollbar();
        mPopupView.setAlpha(0);
        
        mViewHelper.addOnPreDrawListener(this :: onPreDraw);
        mViewHelper.addOnScrollChangedListener(this :: onScrollChanged);
        mViewHelper.addOnTouchEventListener(this :: onTouchEvent);
    }
    
    public void setPadding(int left, int top, int right, int bottom) {
        if (mUserPadding != null && mUserPadding.left == left && mUserPadding.top == top
                && mUserPadding.right == right && mUserPadding.bottom == bottom) {
            return;
        }
        if (mUserPadding == null) {
            mUserPadding = new Rect();
        }
        mUserPadding.set(left, top, right, bottom);
        mView.invalidate();
    }
    
    @NonNull
    private Rect getPadding() {
        if (mUserPadding != null) {
            mTempRect.set(mUserPadding);
        }
        else {
            mTempRect.set(mView.getPaddingLeft(), mView.getPaddingTop(), mView.getPaddingRight(),
                    mView.getPaddingBottom());
        }
        return mTempRect;
    }
    
    public void setPadding(@Nullable Rect padding) {
        if (Objects.equals(mUserPadding, padding)) {
            return;
        }
        if (padding != null) {
            if (mUserPadding == null) {
                mUserPadding = new Rect();
            }
            mUserPadding.set(padding);
        }
        else {
            mUserPadding = null;
        }
        mView.invalidate();
    }
    
    private void onPreDraw() {
        
        updateScrollbarState();
        mTrackView.setVisibility(mScrollbarEnabled ? View.VISIBLE : View.INVISIBLE);
        mThumbView.setVisibility(mScrollbarEnabled ? View.VISIBLE : View.INVISIBLE);
        if (!mScrollbarEnabled) {
            mPopupView.setVisibility(View.INVISIBLE);
            return;
        }
        
        int layoutDirection = mView.getLayoutDirection();
        mTrackView.setLayoutDirection(layoutDirection);
        mThumbView.setLayoutDirection(layoutDirection);
        mPopupView.setLayoutDirection(layoutDirection);
        
        boolean isLayoutRtl = layoutDirection == View.LAYOUT_DIRECTION_RTL;
        int viewWidth = mView.getWidth();
        int viewHeight = mView.getHeight();
        
        Rect padding = getPadding();
        int trackLeft = isLayoutRtl ? padding.left : viewWidth - padding.right - mTrackWidth;
        layoutView(mTrackView, trackLeft, padding.top, trackLeft + mTrackWidth,
                viewHeight - padding.bottom);
        int thumbLeft = isLayoutRtl ? padding.left : viewWidth - padding.right - mThumbWidth;
        int thumbTop = padding.top + mThumbOffset;
        layoutView(mThumbView, thumbLeft, thumbTop, thumbLeft + mThumbWidth,
                thumbTop + mThumbHeight);
        
        String popupText = mViewHelper.getPopupText();
        boolean hasPopup = !TextUtils.isEmpty(popupText);
        mPopupView.setVisibility(hasPopup ? View.VISIBLE : View.INVISIBLE);
        if (hasPopup) {
            FrameLayout.LayoutParams popupLayoutParams = (FrameLayout.LayoutParams)
                    mPopupView.getLayoutParams();
            if (!Objects.equals(mPopupView.getText(), popupText)) {
                mPopupView.setText(popupText);
                int widthMeasureSpec = ViewGroup.getChildMeasureSpec(
                        View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.EXACTLY),
                        padding.left + padding.right + mThumbWidth + popupLayoutParams.leftMargin
                                + popupLayoutParams.rightMargin, popupLayoutParams.width);
                int heightMeasureSpec = ViewGroup.getChildMeasureSpec(
                        View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY),
                        padding.top + padding.bottom + popupLayoutParams.topMargin
                                + popupLayoutParams.bottomMargin, popupLayoutParams.height);
                mPopupView.measure(widthMeasureSpec, heightMeasureSpec);
            }
            int popupWidth = mPopupView.getMeasuredWidth();
            int popupHeight = mPopupView.getMeasuredHeight();
            int popupLeft = isLayoutRtl ? padding.left + mThumbWidth + popupLayoutParams.leftMargin
                    : viewWidth - padding.right - mThumbWidth - popupLayoutParams.rightMargin
                    - popupWidth;
            int popupAnchorY;
            if ((popupLayoutParams.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
                popupAnchorY = popupHeight / 2;
            }
            else {
                popupAnchorY = 0;
            }
            int thumbAnchorY;
            switch (popupLayoutParams.gravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.TOP:
                default:
                    thumbAnchorY = mThumbView.getPaddingTop();
                    break;
                case Gravity.CENTER_VERTICAL: {
                    int thumbPaddingTop = mThumbView.getPaddingTop();
                    thumbAnchorY = thumbPaddingTop + (mThumbHeight - thumbPaddingTop
                            - mThumbView.getPaddingBottom()) / 2;
                    break;
                }
                case Gravity.BOTTOM:
                    thumbAnchorY = mThumbHeight - mThumbView.getPaddingBottom();
                    break;
            }
            int popupTop = MathUtils.clamp(thumbTop + thumbAnchorY - popupAnchorY,
                    padding.top + popupLayoutParams.topMargin,
                    viewHeight - padding.bottom - popupLayoutParams.bottomMargin - popupHeight);
            layoutView(mPopupView, popupLeft, popupTop, popupLeft + popupWidth,
                    popupTop + popupHeight);
        }
    }
    
    private void updateScrollbarState() {
        int scrollOffsetRange = getScrollOffsetRange();
        mScrollbarEnabled = scrollOffsetRange > 0;
        mThumbOffset = mScrollbarEnabled ? (int) ((long) getThumbOffsetRange()
                * mViewHelper.getScrollOffset() / scrollOffsetRange) : 0;
    }
    
    private void layoutView(@NonNull View view, int left, int top, int right, int bottom) {
        int scrollX = mView.getScrollX();
        int scrollY = mView.getScrollY();
        view.layout(scrollX + left, scrollY + top, scrollX + right, scrollY + bottom);
    }
    
    private void onScrollChanged() {
        updateScrollbarState();
        if (!mScrollbarEnabled) {
            return;
        }
        
        mAnimationHelper.showScrollbar(mTrackView, mThumbView);
        postAutoHideScrollbar();
    }
    
    private boolean onTouchEvent(@NonNull MotionEvent event) {
        
        if (!mScrollbarEnabled) {
            return false;
        }
        
        float eventX = event.getX();
        float eventY = event.getY();
        Rect padding = getPadding();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = eventX;
                mDownY = eventY;
                
                if (mTrackView.getAlpha() > 0 && isInView(mTrackView, eventX, eventY)) {
                    mDragStartY = eventY;
                    if (isInViewTouchTarget(mThumbView, eventX, eventY)) {
                        mDragStartThumbOffset = mThumbOffset;
                    }
                    else {
                        mDragStartThumbOffset = (int) (eventY - padding.top - mThumbHeight / 2f);
                        scrollToThumbOffset(mDragStartThumbOffset);
                    }
                    setDragging(true);
                }
    
                mThumbView.animate()
                        .scaleX(0.75F)
                        .scaleY(0.75F)
                        .setInterpolator(new DecelerateInterpolator(1.5F))
                        .setDuration(mThumbView.getContext().getResources().getInteger(R.integer.animation_duration))
                        .start();
    
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mDragging && isInViewTouchTarget(mTrackView, mDownX, mDownY)
                        && Math.abs(eventY - mDownY) > mTouchSlop) {
                    if (isInViewTouchTarget(mThumbView, mDownX, mDownY)) {
                        mDragStartY = mLastY;
                        mDragStartThumbOffset = mThumbOffset;
                    }
                    else {
                        mDragStartY = eventY;
                        mDragStartThumbOffset = (int) (eventY - padding.top - mThumbHeight / 2f);
                        scrollToThumbOffset(mDragStartThumbOffset);
                    }
            
                    setDragging(true);
                }
        
                if (mDragging) {
                    int thumbOffset = mDragStartThumbOffset + (int) (eventY - mDragStartY);
                    scrollToThumbOffset(thumbOffset);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
        
                mThumbView.animate()
                        .scaleX(1.0F)
                        .scaleY(1.0F)
                        .setInterpolator(new DecelerateInterpolator(1.5F))
                        .setDuration(mThumbView.getContext().getResources().getInteger(R.integer.animation_duration))
                        .start();
        
                setDragging(false);
                break;
        }
        
        mLastY = eventY;
        
        return mDragging;
    }
    
    private boolean isInView(@NonNull View view, float x, float y) {
        int scrollX = mView.getScrollX();
        int scrollY = mView.getScrollY();
        return x >= view.getLeft() - scrollX && x < view.getRight() - scrollX
                && y >= view.getTop() - scrollY && y < view.getBottom() - scrollY;
    }
    
    private boolean isInViewTouchTarget(@NonNull View view, float x, float y) {
        int scrollX = mView.getScrollX();
        int scrollY = mView.getScrollY();
        return isInTouchTarget(x, view.getLeft() - scrollX, view.getRight() - scrollX,
                mView.getWidth())
                && isInTouchTarget(y, view.getTop() - scrollY, view.getBottom() - scrollY,
                mView.getHeight());
    }
    
    private boolean isInTouchTarget(float position, int viewStart, int viewEnd, int parentEnd) {
        int viewSize = viewEnd - viewStart;
        if (viewSize >= mMinTouchTargetSize) {
            return position >= viewStart && position < viewEnd;
        }
        int touchTargetStart = viewStart - (mMinTouchTargetSize - viewSize) / 2;
        if (touchTargetStart < 0) {
            touchTargetStart = 0;
        }
        int touchTargetEnd = touchTargetStart + mMinTouchTargetSize;
        if (touchTargetEnd > parentEnd) {
            touchTargetEnd = parentEnd;
            touchTargetStart = touchTargetEnd - mMinTouchTargetSize;
            if (touchTargetStart < 0) {
                touchTargetStart = 0;
            }
        }
        return position >= touchTargetStart && position < touchTargetEnd;
    }
    
    private void scrollToThumbOffset(int thumbOffset) {
        int thumbOffsetRange = getThumbOffsetRange();
        thumbOffset = MathUtils.clamp(thumbOffset, 0, thumbOffsetRange);
        int scrollOffset = (int) ((long) getScrollOffsetRange() * thumbOffset / thumbOffsetRange);
        mViewHelper.scrollTo(scrollOffset);
    }
    
    private int getScrollOffsetRange() {
        return mViewHelper.getScrollRange() - mView.getHeight();
    }
    
    private int getThumbOffsetRange() {
        Rect padding = getPadding();
        return mView.getHeight() - padding.top - padding.bottom - mThumbHeight;
    }
    
    private void setDragging(boolean dragging) {
    
        /*
         * This will prevent the loading of images when scroller
         * is being dragged to allow room for smoother scrolling
         * and not load unnecessary resources.
         */
        if (dragging) {
            Glide.with(mView.getContext()).pauseRequests();
        } else {
            Glide.with(mView.getContext()).resumeRequests();
        }
    
        if (mDragging == dragging) {
            return;
        }
        mDragging = dragging;
    
        if (mDragging) {
            mView.getParent().requestDisallowInterceptTouchEvent(true);
        }
        
        mTrackView.setPressed(mDragging);
        mThumbView.setPressed(mDragging);
        
        if (mDragging) {
            cancelAutoHideScrollbar();
            mAnimationHelper.showScrollbar(mTrackView, mThumbView);
            mAnimationHelper.showPopup(mPopupView);
        }
        else {
            postAutoHideScrollbar();
            mAnimationHelper.hidePopup(mPopupView);
        }
    }
    
    private void postAutoHideScrollbar() {
        cancelAutoHideScrollbar();
        if (mAnimationHelper.isScrollbarAutoHideEnabled()) {
            mView.postDelayed(mAutoHideScrollbarRunnable,
                    mAnimationHelper.getScrollbarAutoHideDelayMillis());
        }
    }
    
    private void autoHideScrollbar() {
        if (mDragging) {
            return;
        }
        mAnimationHelper.hideScrollbar(mTrackView, mThumbView);
    }
    
    private void cancelAutoHideScrollbar() {
        mView.removeCallbacks(mAutoHideScrollbarRunnable);
    }
    
    public interface ViewHelper {
        void addOnPreDrawListener(@NonNull Runnable onPreDraw);
        
        void addOnScrollChangedListener(@NonNull Runnable onScrollChanged);
        
        void addOnTouchEventListener(@NonNull Predicate <MotionEvent> onTouchEvent);
        
        int getScrollRange();
        
        int getScrollOffset();
        
        void scrollTo(int offset);
        
        @Nullable
        default String getPopupText() {
            return null;
        }
    }
    
    public interface AnimationHelper {
        void showScrollbar(@NonNull View trackView, @NonNull View thumbView);
        
        void hideScrollbar(@NonNull View trackView, @NonNull View thumbView);
        
        boolean isScrollbarAutoHideEnabled();
        
        int getScrollbarAutoHideDelayMillis();
        
        void showPopup(@NonNull View popupView);
        
        void hidePopup(@NonNull View popupView);
    }
}
