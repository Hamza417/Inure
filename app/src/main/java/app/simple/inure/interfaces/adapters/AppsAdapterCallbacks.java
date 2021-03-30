package app.simple.inure.interfaces.adapters;

import android.content.pm.ApplicationInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

public interface AppsAdapterCallbacks {
    default void onAppClicked(@NotNull ApplicationInfo applicationInfo, @NotNull ImageView icon) {
    
    }
    
    default void onAppLongPress(@NotNull ApplicationInfo applicationInfo, @NotNull ViewGroup viewGroup, float xOff, float yOff, ImageView icon) {
    
    }
    
    default void onSearchPressed(@NotNull View view) {
    
    }
    
    default void onSettingsPressed() {
    
    }
    
    default void onPrefsIconPressed(@NotNull View view) {
    
    }
}
