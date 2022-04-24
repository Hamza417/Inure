package app.simple.inure.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import app.simple.inure.decorations.theme.ThemeLinearLayout;
import app.simple.inure.util.ViewUtils;

public class DynamicCornerLinearLayoutWithFactor extends ThemeLinearLayout {
    public DynamicCornerLinearLayoutWithFactor(Context context) {
        super(context);
        init(null);
        setOrientation(LinearLayout.VERTICAL);
    }
    
    public DynamicCornerLinearLayoutWithFactor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    
    public DynamicCornerLinearLayoutWithFactor(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attributeSet) {
        LayoutBackground.setBackground(getContext(), this, attributeSet, 2F);
        ViewUtils.INSTANCE.addShadow(this);
    }
}
