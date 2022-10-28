package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import app.simple.inure.constants.Misc;
import app.simple.inure.preferences.AppearancePreferences;

public class AppIconImageView extends AppCompatImageView implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    private int size = 0;
    
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
        if (isInEditMode()) {
            size = Misc.appIconsDimension;
        } else {
            size = AppearancePreferences.INSTANCE.getIconSize();
        }
        // updateLayout(AppearancePreferences.INSTANCE.getIconSize());
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            app.simple.inure.preferences.SharedPreferences.INSTANCE.registerListener(this);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            app.simple.inure.preferences.SharedPreferences.INSTANCE.unregisterListener(this);
        }
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
