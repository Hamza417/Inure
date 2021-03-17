package app.simple.inure.decorations.transitions;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.core.view.ViewCompat;
import androidx.transition.SidePropagation;
import androidx.transition.TransitionValues;
import androidx.transition.Visibility;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

public class Slide extends Visibility {
    
    private static final TimeInterpolator sDecelerate = new DecelerateInterpolator(1.5F);
    private static final TimeInterpolator sAccelerate = new AccelerateInterpolator(1.5F);
    private static final String PROPNAME_SCREEN_POSITION = "android:slide:screenPosition";
    private CalculateSlide mSlideCalculator = sCalculateBottom;
    private int mSlideEdge = Gravity.BOTTOM;
    
    private interface CalculateSlide {
        
        /**
         * Returns the translation value for view when it goes out of the scene
         */
        float getGoneX(ViewGroup sceneRoot, View view);
        
        /**
         * Returns the translation value for view when it goes out of the scene
         */
        float getGoneY(ViewGroup sceneRoot, View view);
    }
    
    @RestrictTo (LIBRARY_GROUP_PREFIX)
    @Retention (RetentionPolicy.SOURCE)
    @IntDef ({Gravity.LEFT, Gravity.TOP, Gravity.RIGHT, Gravity.BOTTOM, Gravity.START, Gravity.END})
    public @interface GravityFlag {
    }
    
    private abstract static class CalculateSlideHorizontal implements CalculateSlide {
        @Override
        public float getGoneY(ViewGroup sceneRoot, View view) {
            return view.getTranslationY();
        }
    }
    
    private abstract static class CalculateSlideVertical implements CalculateSlide {
        @Override
        public float getGoneX(ViewGroup sceneRoot, View view) {
            return view.getTranslationX();
        }
    }
    
    private static final CalculateSlide sCalculateLeft = new CalculateSlideHorizontal() {
        @Override
        public float getGoneX(ViewGroup sceneRoot, View view) {
            return view.getTranslationX() - sceneRoot.getWidth();
        }
    };
    
    private static final CalculateSlide sCalculateStart = new CalculateSlideHorizontal() {
        @Override
        public float getGoneX(ViewGroup sceneRoot, View view) {
            final boolean isRtl = ViewCompat.getLayoutDirection(sceneRoot) == ViewCompat.LAYOUT_DIRECTION_RTL;
            final float x;
            
            if (isRtl) {
                x = view.getTranslationX() + sceneRoot.getWidth();
            }
            else {
                x = view.getTranslationX() - sceneRoot.getWidth();
            }
            return x;
        }
    };
    
    private static final CalculateSlide sCalculateTop = new CalculateSlideVertical() {
        @Override
        public float getGoneY(ViewGroup sceneRoot, View view) {
            return view.getTranslationY() - sceneRoot.getHeight();
        }
    };
    
    private static final CalculateSlide sCalculateRight = new CalculateSlideHorizontal() {
        @Override
        public float getGoneX(ViewGroup sceneRoot, View view) {
            return view.getTranslationX() + sceneRoot.getWidth();
        }
    };
    
    private static final CalculateSlide sCalculateEnd = new CalculateSlideHorizontal() {
        @Override
        public float getGoneX(ViewGroup sceneRoot, View view) {
            final boolean isRtl = ViewCompat.getLayoutDirection(sceneRoot) == ViewCompat.LAYOUT_DIRECTION_RTL;
            final float x;
            
            if (isRtl) {
                x = view.getTranslationX() - sceneRoot.getWidth();
            }
            else {
                x = view.getTranslationX() + sceneRoot.getWidth();
            }
            return x;
        }
    };
    
    private static final CalculateSlide sCalculateBottom = new CalculateSlideVertical() {
        @Override
        public float getGoneY(ViewGroup sceneRoot, View view) {
            return view.getTranslationY() + sceneRoot.getHeight();
        }
    };
    
    /**
     * Constructor using the default {@link Gravity#BOTTOM}
     * slide edge direction.
     */
    public Slide() {
        setSlideEdge(Gravity.BOTTOM);
    }
    
    /**
     * Constructor using the provided slide edge direction.
     */
    public Slide(@GravityFlag int slideEdge) {
        setSlideEdge(slideEdge);
    }
    
    @SuppressLint ("RestrictedApi") // remove once core lib would be released with the new
    // LIBRARY_GROUP_PREFIX restriction. tracking in b/127286008
    public Slide(Context context, AttributeSet attrs) {
        super(context, attrs);
        int edge = Gravity.BOTTOM;
        //noinspection WrongConstant
        setSlideEdge(edge);
    }
    
    private void captureValues(TransitionValues transitionValues) {
        View view = transitionValues.view;
        int[] position = new int[2];
        view.getLocationOnScreen(position);
        transitionValues.values.put(PROPNAME_SCREEN_POSITION, position);
    }
    
    @Override
    public void captureStartValues(@NonNull TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        captureValues(transitionValues);
    }
    
    @Override
    public void captureEndValues(@NonNull TransitionValues transitionValues) {
        super.captureEndValues(transitionValues);
        captureValues(transitionValues);
    }
    
    /**
     * Change the edge that Views appear and disappear from.
     *
     * @param slideEdge The edge of the scene to use for Views appearing and disappearing. One of
     *                  {@link android.view.Gravity#LEFT}, {@link android.view.Gravity#TOP},
     *                  {@link android.view.Gravity#RIGHT}, {@link android.view.Gravity#BOTTOM},
     *                  {@link android.view.Gravity#START}, {@link android.view.Gravity#END}.
     */
    public void setSlideEdge(@androidx.transition.Slide.GravityFlag int slideEdge) {
        switch (slideEdge) {
            case Gravity.LEFT:
                mSlideCalculator = sCalculateLeft;
                break;
            case Gravity.TOP:
                mSlideCalculator = sCalculateTop;
                break;
            case Gravity.RIGHT:
                mSlideCalculator = sCalculateRight;
                break;
            case Gravity.BOTTOM:
                mSlideCalculator = sCalculateBottom;
                break;
            case Gravity.START:
                mSlideCalculator = sCalculateStart;
                break;
            case Gravity.END:
                mSlideCalculator = sCalculateEnd;
                break;
            default:
                throw new IllegalArgumentException("Invalid slide direction");
        }
        mSlideEdge = slideEdge;
        SidePropagation propagation = new SidePropagation();
        propagation.setSide(slideEdge);
        setPropagation(propagation);
    }
    
    /**
     * Returns the edge that Views appear and disappear from.
     *
     * @return the edge of the scene to use for Views appearing and disappearing. One of
     * {@link android.view.Gravity#LEFT}, {@link android.view.Gravity#TOP},
     * {@link android.view.Gravity#RIGHT}, {@link android.view.Gravity#BOTTOM},
     * {@link android.view.Gravity#START}, {@link android.view.Gravity#END}.
     */
    @GravityFlag
    public int getSlideEdge() {
        return mSlideEdge;
    }
    
    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view,
            TransitionValues startValues, TransitionValues endValues) {
        if (endValues == null) {
            return null;
        }
        int[] position = (int[]) endValues.values.get(PROPNAME_SCREEN_POSITION);
        float endX = view.getTranslationX();
        float endY = view.getTranslationY();
        float startX = mSlideCalculator.getGoneX(sceneRoot, view);
        float startY = mSlideCalculator.getGoneY(sceneRoot, view);
        assert position != null;
        return TranslationAnimationCreator
                .createAnimation(view, endValues, position[0], position[1],
                        startX, startY, endX, endY, sDecelerate, this);
    }
    
    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view,
            TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null) {
            return null;
        }
        int[] position = (int[]) startValues.values.get(PROPNAME_SCREEN_POSITION);
        float startX = view.getTranslationX();
        float startY = view.getTranslationY();
        float endX = mSlideCalculator.getGoneX(sceneRoot, view);
        float endY = mSlideCalculator.getGoneY(sceneRoot, view);
        assert position != null;
        return TranslationAnimationCreator
                .createAnimation(view, startValues, position[0], position[1],
                        startX, startY, endX, endY, sAccelerate, this);
    }
    
}
