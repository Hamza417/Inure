package app.simple.inure.decorations.ripple;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

public class DynamicRippleButton extends androidx.appcompat.widget.AppCompatButton {
    public DynamicRippleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        setBackground(Utils.getRippleDrawable(getContext(), getBackground(), 2F));
    }
}
