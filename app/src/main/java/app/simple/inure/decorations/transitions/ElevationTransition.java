package app.simple.inure.decorations.transitions;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import androidx.transition.Transition;
import androidx.transition.TransitionValues;

@TargetApi (Build.VERSION_CODES.LOLLIPOP)
public class ElevationTransition extends Transition {
    
    private static final String PROPNAME_ELEVATION = "my.elevation:transition:elevation";
    
    public ElevationTransition() {
    }
    
    public ElevationTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public void captureStartValues(@NotNull TransitionValues transitionValues) {
        captureValues(transitionValues);
    }
    
    @Override
    public void captureEndValues(@NotNull TransitionValues transitionValues) {
        captureValues(transitionValues);
    }
    
    private void captureValues(TransitionValues transitionValues) {
        Float elevation = transitionValues.view.getElevation();
        transitionValues.values.put(PROPNAME_ELEVATION, elevation);
    }
    
    @Override
    public Animator createAnimator(@NotNull ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }
        
        Float startVal = (Float) startValues.values.get(PROPNAME_ELEVATION);
        Float endVal = (Float) endValues.values.get(PROPNAME_ELEVATION);
        if (startVal == null || endVal == null || startVal.floatValue() == endVal.floatValue()) {
            return null;
        }
        
        final View view = endValues.view;
        ValueAnimator a = ValueAnimator.ofFloat(startVal, endVal);
        a.addUpdateListener(animation -> view.setElevation((float) animation.getAnimatedValue()));
        
        return a;
    }
}
