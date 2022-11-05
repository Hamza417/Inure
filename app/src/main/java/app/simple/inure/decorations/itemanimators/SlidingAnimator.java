package app.simple.inure.decorations.itemanimators;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

public class SlidingAnimator extends SimpleItemAnimator {
    
    List <RecyclerView.ViewHolder> pendingAdditions = new ArrayList <>();
    List <RecyclerView.ViewHolder> pendingRemovals = new ArrayList <>();
    
    @Override
    public void runPendingAnimations() {
        final List <RecyclerView.ViewHolder> additionsTmp = pendingAdditions;
        List <RecyclerView.ViewHolder> removalsTmp = pendingRemovals;
        pendingAdditions = new ArrayList <>();
        pendingRemovals = new ArrayList <>();
        
        for (RecyclerView.ViewHolder removal : removalsTmp) {
            animateRemoveImpl(removal); // run the pending remove animation
        }
        removalsTmp.clear();
        
        if (!additionsTmp.isEmpty()) {
            Runnable adder = () -> {
                for (RecyclerView.ViewHolder addition : additionsTmp) {
                    animateAddImpl(addition); // run the pending add animation
                }
                additionsTmp.clear();
            };
            // play the add animation after the remove animation finished
            ViewCompat.postOnAnimationDelayed(additionsTmp.get(0).itemView, adder, getRemoveDuration());
        }
    }
    
    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        pendingAdditions.add(holder);
        // translate the new items vertically so that they later slide in from the bottom
        holder.itemView.setTranslationY(300);
        // also make them invisible
        holder.itemView.setAlpha(0);
        // this requests the execution of runPendingAnimations()
        return true;
    }
    
    @Override
    public boolean animateRemove(final RecyclerView.ViewHolder holder) {
        pendingRemovals.add(holder);
        // this requests the execution of runPendingAnimations()
        return true;
    }
    
    private void animateAddImpl(final RecyclerView.ViewHolder holder) {
        View view = holder.itemView;
        final ViewPropertyAnimatorCompat anim = ViewCompat.animate(view);
        anim.translationY(0) // undo the translation we applied in animateAdd
                .alpha(1) // undo the alpha we applied in animateAdd
                .setDuration(getAddDuration())
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull View view) {
                        dispatchAddStarting(holder);
                    }
                    
                    @Override
                    public void onAnimationEnd(@NonNull View view) {
                        anim.setListener(null);
                        dispatchAddFinished(holder);
                        // cleanup
                        view.setTranslationY(0);
                        view.setAlpha(1);
                    }
                    
                    @Override
                    public void onAnimationCancel(@NonNull View view) {
                    }
                }).start();
    }
    
    private void animateRemoveImpl(final RecyclerView.ViewHolder holder) {
        View view = holder.itemView;
        final ViewPropertyAnimatorCompat anim = ViewCompat.animate(view);
        anim.translationX(view.getWidth()) // translate horizontally to provide slide out to right
                .alpha(0) // fade out
                .setDuration(getRemoveDuration())
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull View view) {
                        dispatchRemoveStarting(holder);
                    }
                    
                    @Override
                    public void onAnimationEnd(@NonNull View view) {
                        anim.setListener(null);
                        dispatchRemoveFinished(holder);
                        // cleanup
                        view.setTranslationX(0);
                        view.setAlpha(1);
                    }
                    
                    @Override
                    public void onAnimationCancel(@NonNull View view) {
                    }
                }).start();
    }
    
    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        // don't handle animateMove because there should only be add/remove animations
        dispatchMoveFinished(holder);
        return false;
    }
    
    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop) {
        // don't handle animateChange because there should only be add/remove animations
        if (newHolder != null) {
            dispatchChangeFinished(newHolder, false);
        }
        dispatchChangeFinished(oldHolder, true);
        return false;
    }
    
    @Override
    public void endAnimation(@NonNull RecyclerView.ViewHolder item) {
    }
    
    @Override
    public void endAnimations() {
    }
    
    @Override
    public boolean isRunning() {
        return false;
    }
}