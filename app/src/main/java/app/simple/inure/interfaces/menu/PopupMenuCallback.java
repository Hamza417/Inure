package app.simple.inure.interfaces.menu;

import android.content.pm.ApplicationInfo;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

public interface PopupMenuCallback {
    void onMenuItemClicked(@NotNull String source, ApplicationInfo applicationInfo, ImageView icon);
}
