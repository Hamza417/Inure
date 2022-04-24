package app.simple.inure.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import app.simple.inure.decorations.theme.ThemeHorizontalScrollView;
import app.simple.inure.util.ViewUtils;

public class DynamicCornerHorizontalScrollViewWithFactor extends ThemeHorizontalScrollView {
    public DynamicCornerHorizontalScrollViewWithFactor(Context context) {
        super(context);
        init(null);
    }
    
    public DynamicCornerHorizontalScrollViewWithFactor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    
    public DynamicCornerHorizontalScrollViewWithFactor(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attributeSet) {
        LayoutBackground.setBackground(getContext(), this, attributeSet, 2F);
        ViewUtils.INSTANCE.addShadow(this);
    }
}
