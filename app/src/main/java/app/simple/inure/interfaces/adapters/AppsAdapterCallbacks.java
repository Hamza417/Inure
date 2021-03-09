package app.simple.inure.interfaces.adapters;

import android.widget.ImageButton;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

public interface AppsAdapterCallbacks {
    void onAppClicked(@NotNull String packageName, @NotNull ImageView icon);
    void onMenuClicked(@NotNull String packageName, ImageButton menu);
}
