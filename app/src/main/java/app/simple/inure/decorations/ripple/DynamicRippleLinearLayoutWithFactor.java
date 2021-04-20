package app.simple.inure.decorations.ripple;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class DynamicRippleLinearLayoutWithFactor extends LinearLayout {
    public DynamicRippleLinearLayoutWithFactor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        setBackground(Utils.getRippleDrawable(getContext(), getBackground(), 2F));
    }
}
