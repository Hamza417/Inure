package app.simple.inure.decorations.animatedbackground;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

public class DynamicRippleImageButton extends androidx.appcompat.widget.AppCompatImageButton {
    public DynamicRippleImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        setBackground(Utils.getRippleDrawable(getContext(), getBackground(), 2F));
    }
}
