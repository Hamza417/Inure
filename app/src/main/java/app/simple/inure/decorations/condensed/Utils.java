package app.simple.inure.decorations.condensed;

import android.content.res.Resources;
import android.view.ViewGroup;

import app.simple.inure.R;
import app.simple.inure.preferences.AppearancePreferences;

class Utils {
    static final int CONDENSED_FACTOR = 8;
    
    static void setMargin(ViewGroup.MarginLayoutParams params, Resources resources) {
        int margin = resources.getDimensionPixelSize(R.dimen.list_item_padding);
        int verticalMargin = getVerticalMargin(resources);
        
        params.setMargins(margin, verticalMargin, margin, verticalMargin);
    }
    
    static int getVerticalMargin(Resources resources) {
        if (AppearancePreferences.INSTANCE.getListStyle() == AppearancePreferences.LIST_STYLE_CONDENSED) {
            return resources.getDimensionPixelSize(R.dimen.list_item_padding) / Utils.CONDENSED_FACTOR;
        }
        
        return resources.getDimensionPixelSize(R.dimen.list_item_padding);
    }
}
