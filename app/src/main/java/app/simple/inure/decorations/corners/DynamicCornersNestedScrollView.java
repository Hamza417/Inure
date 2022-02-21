package app.simple.inure.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.theme.ThemeNestedScrollView;
import app.simple.inure.util.ViewUtils;

public class DynamicCornersNestedScrollView extends ThemeNestedScrollView {
    public DynamicCornersNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    
    public DynamicCornersNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attributeSet) {
        LayoutBackground.setBackground(getContext(), this, attributeSet);
        ViewUtils.INSTANCE.addShadow(this);
    }
}
