package app.simple.inure.decorations.padding;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.util.StatusBarHeight;

public class PaddingAwareFrameLayout extends FrameLayout {
    public PaddingAwareFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }
    
    public PaddingAwareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        setPadding(getPaddingLeft(),
                StatusBarHeight.getStatusBarHeight(getResources()) + getPaddingTop(),
                getPaddingRight(),
                getPaddingBottom());
    }
}
