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
import app.simple.inure.decorations.theme.ThemeLinearLayout;
import app.simple.inure.util.ViewUtils;

public class PaddingAwareLinearLayout extends ThemeLinearLayout implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    public PaddingAwareLinearLayout(Context context) {
        super(context);
        init();
    }
    
    public PaddingAwareLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public PaddingAwareLinearLayout(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        ViewUtils.INSTANCE.applyEdgeToEdge(this, true, false);
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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
