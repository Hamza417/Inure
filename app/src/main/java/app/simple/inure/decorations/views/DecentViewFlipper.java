package app.simple.inure.decorations.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;

public class DecentViewFlipper extends ViewFlipper implements View.OnLayoutChangeListener {
    
    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        onViewFlipperFlippedListener.onViewFlipperFlipped(view, getDisplayedChild());
    }
    
    public interface OnViewFlipperFlippedListener {
        void onViewFlipperFlipped(View childView, int index);
    }
    
    public DecentViewFlipper(Context context) {
        super(context);
    }
    
    public DecentViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    private static final String TAG = DecentViewFlipper.class.getSimpleName();
    private OnViewFlipperFlippedListener onViewFlipperFlippedListener = null;
    
    public void setOnViewFlipperFlippedListener(@NonNull OnViewFlipperFlippedListener listener) {
        if (listener == null) {
            throw new IllegalStateException("OnViewFlipperFlippedListener can not be null");
        }
        if (getChildCount() != 0) {
            DecentViewFlipper flipper = (DecentViewFlipper) getCurrentView().getParent();
            flipper.addOnLayoutChangeListener(this);
        } else {
            throw new IllegalStateException("can't use setOnViewFlipperFlippedListener before you have added at least one View to the " + TAG);
        }
        onViewFlipperFlippedListener = listener;
    }
    
    public OnViewFlipperFlippedListener getOnViewFlipperFlippedListener() {
        return onViewFlipperFlippedListener;
    }
    
    public void removeListeners() {
        onViewFlipperFlippedListener = null;
    }
}