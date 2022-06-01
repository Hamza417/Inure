package app.simple.inure.extensions.popup;

import android.content.pm.ApplicationInfo;

import org.jetbrains.annotations.NotNull;

public interface PopupMenuCallback {
    
    /**
     * Suitable for using with {@link androidx.recyclerview.widget.RecyclerView}
     *
     * @param source          text of the menu item
     * @param applicationInfo {@link ApplicationInfo}
     */
    default void onMenuItemClicked(@NotNull String source, ApplicationInfo applicationInfo) {
    }
    
    /**
     * Suitable for using with dialog fragments
     *
     * @param source text of the menu item
     */
    default void onMenuItemClicked(@NotNull String source) {
    }
    
    /**
     * Suitable for using with dialog fragments
     *
     * @param source text of the menu item
     */
    default void onMenuItemClicked(@NotNull int source) {
    }
    
    /**
     * Suitable for using with {@link app.simple.inure.popups.app.PopupSure} to quickly
     * handle callbacks when sure is pressed.
     */
    default void onSureClicked() {
    }
    
    default void onError(String error) {
    
    }
    
    /**
     * Called when popup is dismissed, use it to
     * trigger animations or any pending events
     */
    default void onDismiss() {
    }
}
