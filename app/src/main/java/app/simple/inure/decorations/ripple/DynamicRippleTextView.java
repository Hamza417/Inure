package app.simple.inure.decorations.ripple;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import app.simple.inure.R;
import app.simple.inure.decorations.typeface.TypeFaceTextView;

/**
 * {@link androidx.appcompat.widget.AppCompatTextView} but with animated
 * background
 */
public class DynamicRippleTextView extends TypeFaceTextView {
    
    private final float divisiveFactor = 1.5F;
    
    public DynamicRippleTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        setBackground(Utils.getRippleDrawable(getContext(), getBackground(), divisiveFactor));
    }
    
    public DynamicRippleTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.TRANSPARENT);
        setBackground(Utils.getRippleDrawable(getContext(), getBackground(), divisiveFactor));
    }
    
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setBackground(Utils.getRoundedBackground(divisiveFactor));
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.textSelected)));
        setClickable(false);
    }
    
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (isSelected()) {
            super.setOnClickListener(null);
        } else {
            super.setOnClickListener(l);
        }
    }
}
