package com.kekstudio.dachshundtablayout;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by Andy671
 */

public class HelperUtils {
    
    public static int pxToDp(float px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, Resources.getSystem().getDisplayMetrics());
    }
    
    public static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
    
}
