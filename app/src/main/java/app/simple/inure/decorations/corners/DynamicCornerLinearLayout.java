package app.simple.inure.decorations.corners;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import app.simple.inure.R;
import app.simple.inure.util.ViewUtils;

public class DynamicCornerLinearLayout extends LinearLayout {
    public DynamicCornerLinearLayout(Context context) {
        super(context);
        init(null);
        setOrientation(LinearLayout.VERTICAL);
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mainBackground)));
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
        LayoutBackground.setBackground(getContext(), this, attributeSet);
        ViewUtils.INSTANCE.addShadow(this);
    }
}
