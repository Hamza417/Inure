package app.simple.inure.decorations.ripple;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import app.simple.inure.decorations.corners.DynamicCornerMaterialCardView;
import app.simple.inure.preferences.AppearancePreferences;

public class DynamicRippleMaterialCardView extends DynamicCornerMaterialCardView {
    
    public DynamicRippleMaterialCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public DynamicRippleMaterialCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        
        setRippleColor(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColor()));
    }
}
