package app.simple.inure.decorations.transitions;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import androidx.transition.Transition;
import androidx.transition.TransitionValues;

public class ScaleTransition extends Transition {
    private final static String PROPNAME_SCALE_X = "PROPNAME_SCALE_X";
    private final static String PROPNAME_SCALE_Y = "PROPNAME_SCALE_Y";
    
    @Override
    public void captureStartValues(@NotNull TransitionValues transitionValues) {
        captureValues(transitionValues);
    }
    
    @Override
    public void captureEndValues(@NotNull TransitionValues transitionValues) {
        captureValues(transitionValues);
    }
    
    private void captureValues(TransitionValues values) {
        values.values.put(PROPNAME_SCALE_X, values.view.getScaleX());
        values.values.put(PROPNAME_SCALE_Y, values.view.getScaleY());
    }
    
    @Override
    public Animator createAnimator(@NotNull ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (endValues == null || startValues == null) {
            return null;    // no values
        }
        
        float startX = (float) startValues.values.get(PROPNAME_SCALE_X);
        float startY = (float) startValues.values.get(PROPNAME_SCALE_Y);
        float endX = (float) endValues.values.get(PROPNAME_SCALE_X);
        float endY = (float) endValues.values.get(PROPNAME_SCALE_Y);
        
        if (startX == endX && startY == endY) {
            return null;    // no scale to run
        }
        
        final View view = startValues.view;
        PropertyValuesHolder propX = PropertyValuesHolder.ofFloat(PROPNAME_SCALE_X, startX, endX);
        PropertyValuesHolder propY = PropertyValuesHolder.ofFloat(PROPNAME_SCALE_Y, startY, endY);
        ValueAnimator valAnim = ValueAnimator.ofPropertyValuesHolder(propX, propY);
        valAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                view.setPivotX(view.getWidth() / 2f);
                view.setPivotY(view.getHeight() / 2f);
                view.setScaleX((float) valueAnimator.getAnimatedValue(PROPNAME_SCALE_X));
                view.setScaleY((float) valueAnimator.getAnimatedValue(PROPNAME_SCALE_Y));
            }
        });
        return valAnim;
    }
}
