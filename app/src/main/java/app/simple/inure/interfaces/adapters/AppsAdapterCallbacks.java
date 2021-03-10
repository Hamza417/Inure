package app.simple.inure.interfaces.adapters;

import android.content.pm.ApplicationInfo;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

public interface AppsAdapterCallbacks {
    void onAppClicked(@NotNull ApplicationInfo applicationInfo, @NotNull ImageView icon);
    void onMenuClicked(@NotNull ApplicationInfo applicationInfo, @NotNull ViewGroup viewGroup, float xOff, float yOff, ImageView icon);
}
