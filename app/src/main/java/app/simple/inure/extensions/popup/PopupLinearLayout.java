package app.simple.inure.extensions.popup;

import android.content.Context;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import app.simple.inure.R;
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout;
import app.simple.inure.preferences.AccessibilityPreferences;

public class PopupLinearLayout extends DynamicCornerLinearLayout {
    public PopupLinearLayout(Context context) {
        super(context);
        init();
    }
    
    public PopupLinearLayout(Context context, int orientation) {
        super(context);
        init(orientation);
    }
    
    private void init() {
        int p = getResources().getDimensionPixelOffset(R.dimen.popup_padding);
        setPadding(p, p, p, p);
        setOrientation(LinearLayout.VERTICAL);
        animateChildren();
    }
    
    private void init(int orientation) {
        int p = getResources().getDimensionPixelOffset(R.dimen.popup_padding);
        setPadding(p, p, p, p);
        setOrientation(orientation);
        animateChildren();
    }
    
    private void animateChildren() {
        if (!AccessibilityPreferences.INSTANCE.isAnimationReduced()) {
            setScaleY(0);
            
            animate()
                    .scaleY(1)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator(1.5F))
                    .start();
            
            post(() -> {
                for (int i = 0; i < getChildCount(); i++) {
                    getChildAt(i).setAlpha(0);
                    getChildAt(i).setTranslationY(-8);
                    
                    getChildAt(i).animate()
                            .translationY(0)
                            .alpha(1)
                            .setDuration(200)
                            .setStartDelay(200 + (i * 35L))
                            .start();
                }
            });
        }
    }
}
