package app.simple.inure.interfaces.menus;

import android.net.Uri;

import org.jetbrains.annotations.NotNull;

public interface PopupMusicMenuCallbacks {
    void onPlay(@NotNull Uri uri);
    
    void onDelete(@NotNull Uri uri);
    
    void onShare(@NotNull Uri uri);
}
