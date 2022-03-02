package app.simple.inure.interfaces.adapters;

import android.content.pm.PackageInfo;
import android.view.View;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

public interface AppsAdapterCallbacks {
    default void onAppClicked(@NotNull PackageInfo packageInfo, @NotNull ImageView icon) {
    
    }
    
    default void onAppLongPressed(@NotNull PackageInfo packageInfo, @NotNull ImageView icon) {
    
    }
    
    default void onSearchPressed(@NotNull View view) {
    
    }
    
    default void onFilterPressed(View view) {
    
    }
    
    default void onSortPressed(View view) {
    
    }
    
    default void onSettingsPressed(@NotNull View view) {
    
    }
    
    default void onItemSelected(int position) {
    
    }
}
