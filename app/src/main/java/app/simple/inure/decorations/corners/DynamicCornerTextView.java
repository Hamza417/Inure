package app.simple.inure.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.typeface.TypeFaceTextView;
import app.simple.inure.util.ViewUtils;

public class DynamicCornerTextView extends TypeFaceTextView {
    public DynamicCornerTextView(@NonNull Context context) {
        super(context);
        init(null);
    }
    
    public DynamicCornerTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    
    public DynamicCornerTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attributeSet) {
        if (!isInEditMode()) {
            LayoutBackground.setBackground(getContext(), this, attributeSet);
            ViewUtils.INSTANCE.addShadow(this);
        }
    }
}
