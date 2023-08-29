package app.simple.inure.decorations.fastscroll;

import android.content.Context;
import android.content.Intent;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import app.simple.inure.R;
import app.simple.inure.decorations.views.BottomMenuRecyclerView;
import app.simple.inure.preferences.DevelopmentPreferences;

public class FastScroller {
    
    private final int mMinTouchTargetSize;
    private final int mTouchSlop;
    
    @NonNull
    private final ViewGroup view;
    @NonNull
    private final ViewHelper mViewHelper;
    @NonNull
    private final AnimationHelper animationHelper;
    private final int mTrackWidth;
    private final int mThumbWidth;
    private final int mThumbHeight;
    @NonNull
    private final View trackView;
    @NonNull
    private final View thumbView;
    @NonNull
    private final TextView popupView;
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
    private boolean dragging;
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
    
        this.view = view;
        mViewHelper = viewHelper;
        mUserPadding = padding;
        this.animationHelper = animationHelper;
    
        mTrackWidth = trackDrawable.getIntrinsicWidth();
        mThumbWidth = thumbDrawable.getIntrinsicWidth();
        mThumbHeight = thumbDrawable.getIntrinsicHeight();
    
        trackView = new View(context);
        trackView.setBackground(trackDrawable);
        thumbView = new View(context);
        thumbView.setBackground(thumbDrawable);
        popupView = new AppCompatTextView(context);
        popupView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        popupStyle.accept(popupView);
    
        ViewGroupOverlay overlay = this.view.getOverlay();
        overlay.add(trackView);
        overlay.add(thumbView);
        overlay.add(popupView);
    
        postAutoHideScrollbar();
        popupView.setAlpha(0);
    
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
        view.invalidate();
    }
    
    @NonNull
    private Rect getPadding() {
        if (mUserPadding != null) {
            mTempRect.set(mUserPadding);
        } else {
            mTempRect.set(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(),
                    view.getPaddingBottom());
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
        } else {
            mUserPadding = null;
        }
        view.invalidate();
    }
    
    private void onPreDraw() {
    
        updateScrollbarState();
        trackView.setVisibility(mScrollbarEnabled ? View.VISIBLE : View.INVISIBLE);
        thumbView.setVisibility(mScrollbarEnabled ? View.VISIBLE : View.INVISIBLE);
        if (!mScrollbarEnabled) {
            popupView.setVisibility(View.INVISIBLE);
            return;
        }
    
        int layoutDirection = view.getLayoutDirection();
        trackView.setLayoutDirection(layoutDirection);
        thumbView.setLayoutDirection(layoutDirection);
        popupView.setLayoutDirection(layoutDirection);
    
        boolean isLayoutRtl = layoutDirection == View.LAYOUT_DIRECTION_RTL;
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
    
        Rect padding = getPadding();
        int trackLeft = isLayoutRtl ? padding.left : viewWidth - padding.right - mTrackWidth;
        layoutView(trackView, trackLeft, padding.top, trackLeft + mTrackWidth,
                viewHeight - padding.bottom);
        int thumbLeft = isLayoutRtl ? padding.left : viewWidth - padding.right - mThumbWidth;
        int thumbTop = padding.top + mThumbOffset;
        layoutView(thumbView, thumbLeft, thumbTop, thumbLeft + mThumbWidth,
                thumbTop + mThumbHeight);
        
        String popupText = mViewHelper.getPopupText();
        boolean hasPopup = !TextUtils.isEmpty(popupText);
        popupView.setVisibility(hasPopup ? View.VISIBLE : View.INVISIBLE);
        if (hasPopup) {
            FrameLayout.LayoutParams popupLayoutParams = (FrameLayout.LayoutParams)
                    popupView.getLayoutParams();
            if (!Objects.equals(popupView.getText(), popupText)) {
                popupView.setText(popupText);
                int widthMeasureSpec = ViewGroup.getChildMeasureSpec(
                        View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.EXACTLY),
                        padding.left + padding.right + mThumbWidth + popupLayoutParams.leftMargin
                                + popupLayoutParams.rightMargin, popupLayoutParams.width);
                int heightMeasureSpec = ViewGroup.getChildMeasureSpec(
                        View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY),
                        padding.top + padding.bottom + popupLayoutParams.topMargin
                                + popupLayoutParams.bottomMargin, popupLayoutParams.height);
                popupView.measure(widthMeasureSpec, heightMeasureSpec);
            }
            int popupWidth = popupView.getMeasuredWidth();
            int popupHeight = popupView.getMeasuredHeight();
            int popupLeft = isLayoutRtl ? padding.left + mThumbWidth + popupLayoutParams.leftMargin
                    : viewWidth - padding.right - mThumbWidth - popupLayoutParams.rightMargin
                    - popupWidth;
            int popupAnchorY;
            if ((popupLayoutParams.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
                popupAnchorY = popupHeight / 2;
            } else {
                popupAnchorY = 0;
            }
            int thumbAnchorY;
            switch (popupLayoutParams.gravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.TOP:
                default:
                    thumbAnchorY = thumbView.getPaddingTop();
                    break;
                case Gravity.CENTER_VERTICAL: {
                    int thumbPaddingTop = thumbView.getPaddingTop();
                    thumbAnchorY = thumbPaddingTop + (mThumbHeight - thumbPaddingTop
                            - thumbView.getPaddingBottom()) / 2;
                    break;
                }
                case Gravity.BOTTOM:
                    thumbAnchorY = mThumbHeight - thumbView.getPaddingBottom();
                    break;
            }
            int popupTop = MathUtils.clamp(thumbTop + thumbAnchorY - popupAnchorY,
                    padding.top + popupLayoutParams.topMargin,
                    viewHeight - padding.bottom - popupLayoutParams.bottomMargin - popupHeight);
            layoutView(popupView, popupLeft, popupTop, popupLeft + popupWidth,
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
        int scrollX = this.view.getScrollX();
        int scrollY = this.view.getScrollY();
        view.layout(scrollX + left, scrollY + top, scrollX + right, scrollY + bottom);
    }
    
    private void onScrollChanged() {
        updateScrollbarState();
        if (!mScrollbarEnabled) {
            return;
        }
    
        animationHelper.showScrollbar(trackView, thumbView);
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
    
                if (trackView.getAlpha() > 0 && isInView(trackView, eventX, eventY)) {
                    mDragStartY = eventY;
                    if (isInViewTouchTarget(thumbView, eventX, eventY)) {
                        mDragStartThumbOffset = mThumbOffset;
                    } else {
                        mDragStartThumbOffset = (int) (eventY - padding.top - mThumbHeight / 2f);
                        scrollToThumbOffset(mDragStartThumbOffset);
                    }
                    setDragging(true);
                }
    
                thumbView.animate()
                        .scaleX(0.75F)
                        .scaleY(0.75F)
                        .setInterpolator(new DecelerateInterpolator(1.5F))
                        .setDuration(thumbView.getContext().getResources().getInteger(R.integer.animation_duration))
                        .start();
    
                break;
            case MotionEvent.ACTION_MOVE:
                if (!dragging && isInViewTouchTarget(trackView, mDownX, mDownY)
                        && Math.abs(eventY - mDownY) > mTouchSlop) {
                    if (isInViewTouchTarget(thumbView, mDownX, mDownY)) {
                        mDragStartY = mLastY;
                        mDragStartThumbOffset = mThumbOffset;
                    } else {
                        mDragStartY = eventY;
                        mDragStartThumbOffset = (int) (eventY - padding.top - mThumbHeight / 2f);
                        scrollToThumbOffset(mDragStartThumbOffset);
                    }
    
                    setDragging(true);
                }
    
                if (dragging) {
                    int thumbOffset = mDragStartThumbOffset + (int) (eventY - mDragStartY);
                    scrollToThumbOffset(thumbOffset);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
    
                thumbView.animate()
                        .scaleX(1.0F)
                        .scaleY(1.0F)
                        .setInterpolator(new DecelerateInterpolator(1.5F))
                        .setDuration(thumbView.getContext().getResources().getInteger(R.integer.animation_duration))
                        .start();
    
                setDragging(false);
                break;
        }
        
        mLastY = eventY;
    
        return dragging;
    }
    
    private boolean isInView(@NonNull View view, float x, float y) {
        int scrollX = this.view.getScrollX();
        int scrollY = this.view.getScrollY();
        return x >= view.getLeft() - scrollX && x < view.getRight() - scrollX
                && y >= view.getTop() - scrollY && y < view.getBottom() - scrollY;
    }
    
    private boolean isInViewTouchTarget(@NonNull View view, float x, float y) {
        int scrollX = this.view.getScrollX();
        int scrollY = this.view.getScrollY();
        return isInTouchTarget(x, view.getLeft() - scrollX, view.getRight() - scrollX,
                this.view.getWidth())
                && isInTouchTarget(y, view.getTop() - scrollY, view.getBottom() - scrollY,
                this.view.getHeight());
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
        return mViewHelper.getScrollRange() - view.getHeight();
    }
    
    private int getScrollOffsetRange(int position) {
        return mViewHelper.getScrollRange() - view.getHeight();
    }
    
    private int getThumbOffsetRange() {
        Rect padding = getPadding();
        return view.getHeight() - padding.top - padding.bottom - mThumbHeight;
    }
    
    private void setDragging(boolean dragging) {
    
        /*
         * This will prevent the loading of images when scroller
         * is being dragged to allow room for smoother scrolling
         * and not load unnecessary resources.
         */
        if (DevelopmentPreferences.INSTANCE.get(DevelopmentPreferences.pauseImageLoader)) {
            if (dragging) {
                Glide.with(view.getContext()).pauseRequests();
            } else {
                Glide.with(view.getContext()).resumeRequests();
            }
        }
    
        if (this.dragging == dragging) {
            return;
        }
    
        this.dragging = dragging;
    
        if (this.dragging) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
        }
    
        trackView.setPressed(this.dragging);
        thumbView.setPressed(this.dragging);
    
        if (this.dragging) {
            cancelAutoHideScrollbar();
            animationHelper.showScrollbar(trackView, thumbView);
            animationHelper.showPopup(popupView);
            Intent intent = new Intent(BottomMenuRecyclerView.ACTION_CLOSE_BOTTOM_MENU);
            LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intent);
        } else {
            postAutoHideScrollbar();
            animationHelper.hidePopup(popupView);
            Intent intent = new Intent(BottomMenuRecyclerView.ACTION_OPEN_BOTTOM_MENU);
            LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intent);
        }
    }
    
    private void postAutoHideScrollbar() {
        cancelAutoHideScrollbar();
        if (animationHelper.isScrollbarAutoHideEnabled()) {
            view.postDelayed(mAutoHideScrollbarRunnable,
                    animationHelper.getScrollbarAutoHideDelayMillis());
        }
    }
    
    private void autoHideScrollbar() {
        if (dragging) {
            return;
        }
        animationHelper.hideScrollbar(trackView, thumbView);
    }
    
    private void cancelAutoHideScrollbar() {
        view.removeCallbacks(mAutoHideScrollbarRunnable);
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
