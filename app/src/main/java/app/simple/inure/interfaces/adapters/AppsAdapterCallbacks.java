package app.simple.inure.interfaces.adapters;

import android.content.pm.ApplicationInfo;
import android.view.View;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

public interface AppsAdapterCallbacks {
    default void onAppClicked(@NotNull ApplicationInfo applicationInfo, @NotNull ImageView icon) {
    
    }
    
    default void onAppLongPress(@NotNull ApplicationInfo applicationInfo, @NotNull View anchor, ImageView icon, int position) {
    
    }
    
    default void onSearchPressed(@NotNull View view) {
    
    }
    
    default void onFilterPressed() {
    
    }
    
    default void onSettingsPressed(@NotNull View view, @NotNull View view1) {
    
    }
    
    default void onItemSelected(int position) {
    
    }
}
