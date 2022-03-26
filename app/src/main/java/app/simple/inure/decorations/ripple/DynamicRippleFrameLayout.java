package app.simple.inure.decorations.ripple;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DynamicRippleFrameLayout extends FrameLayout {
    public DynamicRippleFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }
    
    public DynamicRippleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public DynamicRippleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    public DynamicRippleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    
    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
        setBackground(null);
        setBackground(Utils.getRippleDrawable(getContext(), getBackground()));
    }
}
