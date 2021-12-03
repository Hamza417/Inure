package app.simple.inure.decorations.fastscroll;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

public class Utils {
    @ColorInt
    public static int getColorFromAttrRes(@AttrRes int attrRes, @NonNull Context context) {
        ColorStateList colorStateList = getColorStateListFromAttrRes(attrRes, context);
        return colorStateList != null ? colorStateList.getDefaultColor() : 0;
    }
    
    @Nullable
    public static ColorStateList getColorStateListFromAttrRes(@AttrRes int attrRes, @NonNull Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[] {attrRes});
        int resId;
        try {
            resId = a.getResourceId(0, 0);
            if (resId != 0) {
                return AppCompatResources.getColorStateList(context, resId);
            }
            return a.getColorStateList(0);
        } finally {
            a.recycle();
        }
    }
}
