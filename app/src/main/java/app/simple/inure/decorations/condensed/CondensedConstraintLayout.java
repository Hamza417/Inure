package app.simple.inure.decorations.condensed;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.R;
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout;
import app.simple.inure.preferences.AppearancePreferences;

public class CondensedConstraintLayout extends DynamicRippleConstraintLayout {
    
    /**
     * @noinspection FieldCanBeLocal
     */
    private final int CONDENSED_FACTOR = 4;
    
    public CondensedConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public CondensedConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @Override
    protected void init() {
        super.init();
    }
    
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        setMargin(params instanceof ViewGroup.MarginLayoutParams
                ? (ViewGroup.MarginLayoutParams) params : new ViewGroup.MarginLayoutParams(params));
        super.setLayoutParams(params);
    }
    
    private void setMargin(ViewGroup.MarginLayoutParams params) {
        if (isInEditMode()) {
            return;
        }
        
        int margin = getResources().getDimensionPixelSize(R.dimen.list_item_padding);
        int verticalMargin = getVerticalMargin();
        
        params.setMargins(margin, verticalMargin, margin, verticalMargin);
    }
    
    private int getVerticalMargin() {
        if (AppearancePreferences.INSTANCE.getListStyle() == AppearancePreferences.LIST_STYLE_CONDENSED) {
            return getResources().getDimensionPixelSize(R.dimen.list_item_padding) / CONDENSED_FACTOR;
        }
        
        return getResources().getDimensionPixelSize(R.dimen.list_item_padding);
    }
}
