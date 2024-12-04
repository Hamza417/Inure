package app.simple.inure.decorations.condensed;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout;

import static app.simple.inure.decorations.condensed.Utils.setMargin;

public class CondensedDynamicRippleConstraintLayout extends DynamicRippleConstraintLayout {
    
    public CondensedDynamicRippleConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public CondensedDynamicRippleConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @Override
    protected void init() {
        super.init();
    }
    
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (!isInEditMode()) {
            if (params instanceof ViewGroup.MarginLayoutParams) {
                setMargin((ViewGroup.MarginLayoutParams) params, getResources());
            } else {
                setMargin(new ViewGroup.MarginLayoutParams(params), getResources());
            }
        }
        
        super.setLayoutParams(params);
    }
}
