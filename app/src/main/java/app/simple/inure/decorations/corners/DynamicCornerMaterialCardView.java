package app.simple.inure.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;

import app.simple.inure.decorations.theme.ThemeMaterialCardView;
import app.simple.inure.util.ViewUtils;

public class DynamicCornerMaterialCardView extends ThemeMaterialCardView {
    
    public DynamicCornerMaterialCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public DynamicCornerMaterialCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (isInEditMode()) {
            return;
        }
        
        ViewUtils.INSTANCE.addShadow(this);
    }
}
