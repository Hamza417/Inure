package app.simple.inure.decorations.padding;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import androidx.annotation.Nullable;
import app.simple.inure.R;
import app.simple.inure.decorations.theme.ThemeCoordinatorLayout;
import app.simple.inure.util.ViewUtils;

public class PaddingAwareCoordinatorLayout extends ThemeCoordinatorLayout implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    public PaddingAwareCoordinatorLayout(Context context) {
        super(context);
        init();
    }
    
    public PaddingAwareCoordinatorLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public PaddingAwareCoordinatorLayout(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        ViewUtils.INSTANCE.paddingEdgeToEdge(this);
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @TestOnly
    private void setLayoutTransitions() {
        setLayoutTransition(new LayoutTransition());
        getLayoutTransition().setDuration(getResources().getInteger(R.integer.animation_duration));
        getLayoutTransition().setInterpolator(LayoutTransition.CHANGE_APPEARING, new DecelerateInterpolator(1.5F));
        getLayoutTransition().setInterpolator(LayoutTransition.CHANGE_DISAPPEARING, new DecelerateInterpolator(1.5F));
        getLayoutTransition().setInterpolator(LayoutTransition.CHANGING, new DecelerateInterpolator(1.5F));
        getLayoutTransition().setInterpolator(LayoutTransition.APPEARING, new DecelerateInterpolator(1.5F));
        getLayoutTransition().setInterpolator(LayoutTransition.DISAPPEARING, new DecelerateInterpolator(1.5F));
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    
    }
    
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
