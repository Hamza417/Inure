package app.simple.inure.decorations.transitions;


import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.transition.ArcMotion;
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.ChangeTransform;
import androidx.transition.TransitionSet;

/**
 * Transition that performs almost exactly like {@link android.transition.AutoTransition}, but has an
 * added {@link ChangeImageTransform} to support properly scaling up our gorgeous kittens {@link android.widget.ImageView}.
 *
 * @author bherbst
 */
public class DetailsTransitionArc extends TransitionSet {
    
    public DetailsTransitionArc() {
        init();
    }
    
    /**
     * Set decelerate factor for shared element animation.
     * <p>
     * It is recommended to use lower values for return transition
     * and greater values for enter transition to avoid a quick
     * start of transition and not break the animation
     * patterns
     *
     * @param decelerateFactor should be in range of 0.5F to
     *                         2.0F
     */
    public DetailsTransitionArc(float decelerateFactor) {
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
        /*
         * Arc motion will set a curve on the objects's
         * motion when the view is transitioning.
         */
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMaximumAngle(90);
        arcMotion.setMinimumHorizontalAngle(80);
        arcMotion.setMinimumVerticalAngle(15);
        
        /*
         * Makes sure things go smoothly
         */
        setOrdering(ORDERING_TOGETHER);
        
        /*
         * Setting duration to 750L will force the
         * transition to finish last and not interfere
         * with the other transitions that are going on
         * like RecyclerView's layout transition
         * and AndroidX fragment transitions
         */
        addTransition(new ChangeBounds())
                .addTransition(new ChangeTransform())
                .addTransition(new ChangeImageTransform())
                .setDuration(500L)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setPathMotion(arcMotion);
    }
}
