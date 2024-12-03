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
        setMargin();
    }
    
    private void setMargin() {
        if (!isInEditMode()) {
            return;
        }
        
        int margin = getResources().getDimensionPixelSize(R.dimen.list_item_padding);
        int verticalMargin = getVerticalMargin();
        
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
        params.setMargins(margin, verticalMargin, margin, verticalMargin);
        
        setLayoutParams(params);
    }
    
    private int getVerticalMargin() {
        if (AppearancePreferences.INSTANCE.getListStyle() == AppearancePreferences.LIST_STYLE_CONDENSED) {
            return getResources().getDimensionPixelSize(R.dimen.list_item_padding) / CONDENSED_FACTOR;
        }
        
        return getResources().getDimensionPixelSize(R.dimen.list_item_padding);
    }
}
