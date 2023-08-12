package app.simple.inure.extensions.popup;

import android.content.Context;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import app.simple.inure.R;
import app.simple.inure.decorations.corners.DynamicCornerFrameLayout;
import app.simple.inure.preferences.DevelopmentPreferences;
import app.simple.inure.themes.manager.ThemeManager;

public class PopupFrameLayout extends DynamicCornerFrameLayout {
    public PopupFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }
    
    private void init() {
        if (!DevelopmentPreferences.INSTANCE.get(DevelopmentPreferences.paddingLessPopupMenus)) {
            int p = getResources().getDimensionPixelOffset(R.dimen.popup_padding);
            setPadding(p, p, p, p);
        }
        setBackgroundTintList(ColorStateList.valueOf(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground()));
    }
}
