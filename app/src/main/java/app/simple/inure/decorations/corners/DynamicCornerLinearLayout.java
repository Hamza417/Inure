package app.simple.inure.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import app.simple.inure.decorations.theme.ThemeLinearLayout;
import app.simple.inure.util.ViewUtils;

public class DynamicCornerLinearLayout extends ThemeLinearLayout {
    public DynamicCornerLinearLayout(Context context) {
        super(context);
        init(null);
        setOrientation(LinearLayout.VERTICAL);
    }
    
    public DynamicCornerLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    
    public DynamicCornerLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
