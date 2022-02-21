package app.simple.inure.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.theme.ThemeCoordinatorLayout;
import app.simple.inure.util.ViewUtils;

public class DynamicCornerCoordinatorLayout extends ThemeCoordinatorLayout {
    public DynamicCornerCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(null);
    }
    
    public DynamicCornerCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attributeSet) {
        if (isInEditMode()) {
            return;
        }
        LayoutBackground.setBackground(getContext(), this, attributeSet);
        ViewUtils.INSTANCE.addShadow(this);
    }
}
