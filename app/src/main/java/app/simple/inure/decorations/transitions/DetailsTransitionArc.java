package app.simple.inure.decorations.transitions;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import androidx.transition.ArcMotion;
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.ChangeTransform;
import androidx.transition.TransitionSet;

/**
 * Transition that performs almost exactly like {@link android.transition.AutoTransition}, but has an
 * added {@link ChangeImageTransform} to support properly scaling up our gorgeous kittens.
 *
 * @author bherbst
 */
public class DetailsTransitionArc extends TransitionSet {
    public DetailsTransitionArc() {
        init();
    }
    
    /**
     * This constructor allows us to use this transition in XML
     */
    public DetailsTransitionArc(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMaximumAngle(90);
        arcMotion.setMinimumHorizontalAngle(80);
        arcMotion.setMinimumVerticalAngle(15);
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds())
                .addTransition(new ChangeTransform())
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .setPathMotion(arcMotion);
    }
}
