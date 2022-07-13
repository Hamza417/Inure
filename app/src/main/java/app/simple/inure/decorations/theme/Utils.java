package app.simple.inure.decorations.theme;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import app.simple.inure.R;

public class Utils {
    static ValueAnimator animateBackgroundColor(ViewGroup viewGroup, int endColor) {
        ValueAnimator valueAnimator = ValueAnimator.ofArgb(viewGroup.getBackgroundTintList().getDefaultColor(), endColor);
        valueAnimator.setDuration(viewGroup.getResources().getInteger(R.integer.theme_change_duration));
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(animation -> viewGroup.setBackgroundTintList(ColorStateList.valueOf((int) animation.getAnimatedValue())));
        valueAnimator.start();
        return valueAnimator;
    }
}