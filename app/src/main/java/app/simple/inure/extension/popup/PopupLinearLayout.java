package app.simple.inure.extension.popup;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import app.simple.inure.R;
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout;

public class PopupLinearLayout extends DynamicCornerLinearLayout {
    public PopupLinearLayout(Context context) {
        super(context);
        init();
    }
    
    private void init() {
        int p = getResources().getDimensionPixelOffset(R.dimen.popup_padding);
        setPadding(p, p, p, p);
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.mainBackground)));
        setOrientation(LinearLayout.VERTICAL);
    }
}
