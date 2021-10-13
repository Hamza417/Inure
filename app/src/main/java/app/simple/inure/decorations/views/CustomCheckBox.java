package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatCheckBox;
import app.simple.inure.R;
import app.simple.inure.util.ColorUtils;

public class CustomCheckBox extends AppCompatCheckBox {
    
    public CustomCheckBox(Context context) {
        super(context);
    }
    
    public CustomCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        setButtonTintList(ColorStateList.valueOf(ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent)));
    }
}
