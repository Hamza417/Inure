package app.simple.inure.decorations.padding;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import app.simple.inure.util.StatusBarHeight;

public class PaddingAwareNestedScrollView extends NestedScrollView {
    public PaddingAwareNestedScrollView(@NonNull Context context) {
        super(context);
        init();
    }
    
    public PaddingAwareNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
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
