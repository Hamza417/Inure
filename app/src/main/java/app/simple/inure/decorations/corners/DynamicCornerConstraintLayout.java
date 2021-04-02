package app.simple.inure.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import app.simple.inure.util.ViewUtils;

public class DynamicCornerConstraintLayout extends ConstraintLayout {
    
    public DynamicCornerConstraintLayout(@NonNull Context context) {
        super(context);
        init(null);
    }
    
    public DynamicCornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attributeSet) {
        LayoutBackground.setBackground(getContext(), this, attributeSet);
        ViewUtils.INSTANCE.addShadow(this);
    }
}
