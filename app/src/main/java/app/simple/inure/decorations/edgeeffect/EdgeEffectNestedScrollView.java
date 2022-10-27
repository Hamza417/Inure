package app.simple.inure.decorations.edgeeffect;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EdgeEffect;

import java.lang.reflect.Field;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import app.simple.inure.preferences.AppearancePreferences;

public class EdgeEffectNestedScrollView extends NestedScrollView implements SharedPreferences.OnSharedPreferenceChangeListener {
    public EdgeEffectNestedScrollView(@NonNull Context context) {
        super(context);
        init();
    }
    
    public EdgeEffectNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public EdgeEffectNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setEdgeEffectColor();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }
    
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) {
            return;
        }
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    protected void setEdgeEffectColor() {
        final String[] edgeGlows = {"mEdgeGlowTop", "mEdgeGlowBottom", "mEdgeGlowLeft", "mEdgeGlowRight"};
        for (String edgeGlow : edgeGlows) {
            Class <?> clazz = this.getClass();
            while (clazz != null) {
                try {
                    final Field edgeGlowField = clazz.getDeclaredField(edgeGlow);
                    edgeGlowField.setAccessible(true);
                    final EdgeEffect edgeEffect = (EdgeEffect) edgeGlowField.get(this);
                    edgeEffect.setColor(AppearancePreferences.INSTANCE.getAccentColor());
                    break;
                } catch (Exception e) {
                    // e.printStackTrace();
                    clazz = clazz.getSuperclass();
                }
            }
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Objects.equals(key, AppearancePreferences.accentColor)) {
            setEdgeEffectColor();
        }
    }
}
