package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import app.simple.inure.preferences.AppearancePreferences;

public class AppIconImageView extends AppCompatImageView implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    private int size = AppearancePreferences.INSTANCE.getIconSize();
    
    public AppIconImageView(@NonNull Context context) {
        super(context);
        init();
    }
    
    public AppIconImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public AppIconImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // updateLayout(AppearancePreferences.INSTANCE.getIconSize());
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(size, size);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(AppearancePreferences.iconSize)) {
            setSize(AppearancePreferences.INSTANCE.getIconSize());
        }
    }
    
    public void setSize(int size) {
        this.size = size;
        requestLayout();
    }
}
