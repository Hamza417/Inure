package app.simple.inure.decorations.transitions;

import androidx.transition.Fade;
import androidx.transition.Transition;

public class TransitionManager {
    
    public static final String FADE = "fade";
    public static final String EXPLODE = "explode";
    
    public static Transition getEnterTransitions(String type) {
        switch (type) {
            case EXPLODE: {
                return new Explode();
            }
            case FADE:
            default: {
                return new Fade();
            }
        }
    }
    
    public static Transition getExitTransition(String type) {
        switch (type) {
            case EXPLODE: {
                return new Explode();
            }
            case FADE:
            default: {
                return new Fade();
            }
        }
    }
}
