package app.simple.inure.decorations.condensed;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.ripple.DynamicRippleTextView;

import static app.simple.inure.decorations.condensed.Utils.setMargin;

public class CondensedDynamicRippleTextView extends DynamicRippleTextView {
    public CondensedDynamicRippleTextView(@NonNull Context context) {
        super(context);
    }
    
    public CondensedDynamicRippleTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public CondensedDynamicRippleTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
