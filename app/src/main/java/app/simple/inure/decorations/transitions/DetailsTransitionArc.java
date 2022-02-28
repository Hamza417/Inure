package app.simple.inure.decorations.transitions;


import android.content.Context;
import android.util.AttributeSet;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.transition.ArcMotion;
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.ChangeTransform;
import androidx.transition.TransitionSet;

/**
 * Transition that performs almost exactly like {@link android.transition.AutoTransition}, but has an
 * added {@link ChangeImageTransform} to support properly scaling up {@link android.widget.ImageView}.
 */
public class DetailsTransitionArc extends TransitionSet {
    
    private int maximumAngle = 90;
    private int minimumHorizontalAngle = 80;
    private int minimumVerticalAngle = 15;
    private long duration = 500L;
    
    public DetailsTransitionArc() {
        init();
    }
    
    public DetailsTransitionArc(int maximumAngle, int minimumHorizontalAngle, int minimumVerticalAngle) {
        this.maximumAngle = maximumAngle;
        this.minimumHorizontalAngle = minimumHorizontalAngle;
        this.minimumVerticalAngle = minimumVerticalAngle;
        init();
    }
    
    /**
     * This constructor allows the transition duration to be
     * set dynamically given the distance that view has to travel
     *
     * @param duration duration of the transition
     */
    public DetailsTransitionArc(long duration) {
        this.duration = duration;
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
        arcMotion.setMaximumAngle(maximumAngle);
        arcMotion.setMinimumHorizontalAngle(minimumHorizontalAngle);
        arcMotion.setMinimumVerticalAngle(minimumVerticalAngle);
        
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
                .setDuration(duration)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setPathMotion(arcMotion);
    }
}
