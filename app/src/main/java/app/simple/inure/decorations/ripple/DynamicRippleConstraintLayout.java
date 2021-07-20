package app.simple.inure.decorations.ripple;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import app.simple.inure.R;
import app.simple.inure.decorations.corners.LayoutBackground;
import app.simple.inure.util.ColorUtils;

public class DynamicRippleConstraintLayout extends ConstraintLayout {
    
    public DynamicRippleConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public DynamicRippleConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setBackground(Utils.getRippleDrawable(getContext(), getBackground()));
        setBackgroundColor(Color.TRANSPARENT);
        setDefaultBackground(false);
    }
    
    /**
     * Use this method to track selection in {@link androidx.recyclerview.widget.RecyclerView}.
     * This will change the background according to the accent color and will also keep
     * save the ripple effect.
     *
     * @param selected true for selected item
     */
    public void setDefaultBackground(boolean selected) {
        if (selected) {
            setBackgroundTintList(null);
            setBackgroundTintList(ColorStateList.valueOf(ColorUtils.INSTANCE.changeAlpha(
                    ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent),
                    25)));
            
            LayoutBackground.setBackground(getContext(), this, null);
        }
        else {
            setBackground(null);
            setBackground(Utils.getRippleDrawable(getContext(), getBackground()));
        }
    }
}
