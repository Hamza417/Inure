package app.simple.inure.decorations.itemlistener;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Property;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adds pinch to zoom support to your {@link RecyclerView}, provided it uses a {@link LinearLayoutManager}.
 * <p>
 * Items are translated following the pinch to zoom gesture, and animated back to position in the end.
 * {@link PinchZoomListener} is called whenever a pinch to zoom gesture is performed successfully.
 */
public class PinchZoomItemTouchListener implements RecyclerView.OnItemTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    
    private static final int SETTLE_DURATION_MS = 250;
    private static final Interpolator SETTLE_INTERPOLATOR = new DecelerateInterpolator();
    
    private final ScaleGestureDetector mScaleGestureDetector;
    private final int mSpanSlop;
    private final PinchZoomListener mListener;
    
    private boolean mEnabled = true;
    
    private RecyclerView mRecyclerView;
    private int mOrientation;
    
    private boolean mIntercept;
    
    private float mSpan;
    private int mPosition;
    
    private boolean mDisallowInterceptTouchEvent;
    
    public PinchZoomItemTouchListener(Context context, PinchZoomListener listener) {
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mScaleGestureDetector.setQuickScaleEnabled(false);
        mSpanSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
        mListener = listener;
    }
    
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }
    
    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
        if (!mEnabled) {
            return false;
        }
        
        // Bail out when onRequestDisallowInterceptTouchEvent is called and the motion event has started.
        if (mDisallowInterceptTouchEvent) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mDisallowInterceptTouchEvent = false;
                    break; // Continue handling since down event should always be handled.
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mDisallowInterceptTouchEvent = false;
                default:
                    return false; // Exit now as UP, CANCEL, MOVE and other events shouldn't be handled when disallowed.
            }
        }
        
        // Grab RV reference and current orientation.
        mRecyclerView = recyclerView;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            mOrientation = ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation();
        } else {
            throw new IllegalStateException("PinchZoomItemTouchListener only supports LinearLayoutManager");
        }
        
        // Proxy to ScaleGestureDetector. Its onScaleBegin() method sets mIntercept when called.
        mScaleGestureDetector.onTouchEvent(event);
        return mIntercept;
    }
    
    @Override
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
    }
    
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        mDisallowInterceptTouchEvent = disallowIntercept;
    }
    
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        // Grab the current span and center position of the gesture.
        mSpan = getSpan(detector);
        View child = mRecyclerView.findChildViewUnder(detector.getFocusX(), detector.getFocusY());
        mPosition = child != null ? mRecyclerView.getChildLayoutPosition(child) : RecyclerView.NO_POSITION;
        
        // Determine if we should intercept, based on it being a valid position.
        mIntercept = mPosition != RecyclerView.NO_POSITION;
        
        // Prevent ancestors from intercepting touch events if we will intercept.
        if (mIntercept) {
            ViewParent recyclerViewParent = mRecyclerView.getParent();
            if (recyclerViewParent != null) {
                recyclerViewParent.requestDisallowInterceptTouchEvent(true);
            }
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        // Translate items around the center position.
        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            View child = mRecyclerView.getChildAt(i);
            int position = mRecyclerView.getChildLayoutPosition(child);
            if (position != RecyclerView.NO_POSITION) {
                getTranslateProperty().set(
                        child, Math.max(0, getSpan(detector) - mSpan) * (position < mPosition ? -0.5f : 0.5f));
            }
        }
        
        // Redraw item decorations.
        mRecyclerView.invalidateItemDecorations();
        
        return true;
    }
    
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        // Animate items returning to their resting translations.
        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            final View child = mRecyclerView.getChildAt(i);
            ObjectAnimator animator = ObjectAnimator.ofFloat(child, getTranslateProperty(), 0);
            animator.setDuration(SETTLE_DURATION_MS);
            animator.setInterpolator(SETTLE_INTERPOLATOR);
            animator.start();
            
            // Prevent RV from recycling this child.
            child.setHasTransientState(true);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    child.setHasTransientState(false);
                }
            });
            
            // Redraw item decorations on every frame, but only once per child.
            if (i == 0) {
                animator.addUpdateListener(animation -> mRecyclerView.invalidateItemDecorations());
            }
        }
        
        // Invoke listener if the gesture and position are valid.
        @SuppressWarnings ("rawtypes") RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (mListener != null && getSpan(detector) - mSpan > mSpanSlop
                && adapter != null && mPosition < adapter.getItemCount()) {
            mListener.onPinchZoom(mPosition);
        }
        
        mIntercept = false;
    }
    
    private float getSpan(ScaleGestureDetector detector) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            return detector.getCurrentSpanY();
        } else {
            return detector.getCurrentSpanX();
        }
    }
    
    private Property <View, Float> getTranslateProperty() {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            return View.TRANSLATION_Y;
        } else {
            return View.TRANSLATION_X;
        }
    }
    
    public interface PinchZoomListener {
        void onPinchZoom(int position);
    }
}