package app.simple.inure.decorations.transitions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class AnimatorUtils {
    
    private AnimatorUtils() {
    }
    
    static void addPauseListener(@NonNull Animator animator,
            @NonNull AnimatorListenerAdapter listener) {
        animator.addPauseListener(listener);
    }
    
    static void pause(@NonNull Animator animator) {
        animator.pause();
    }
    
    static void resume(@NonNull Animator animator) {
        animator.resume();
    }
    
    /**
     * Static utility methods for Transitions.
     */
    @Nullable
    public static Animator mergeAnimators(@Nullable Animator animator1, @Nullable Animator animator2) {
        if (animator1 == null) {
            return animator2;
        }
        else if (animator2 == null) {
            return animator1;
        }
        else {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animator1, animator2);
            return animatorSet;
        }
    }
    
    /**
     * Listeners can implement this interface in addition to the platform AnimatorPauseListener to
     * make them compatible with API level 18 and below. Animators will not be paused or resumed,
     * but the callbacks here are invoked.
     */
    interface AnimatorPauseListenerCompat {
        
        void onAnimationPause(Animator animation);
        
        void onAnimationResume(Animator animation);
        
    }
}

