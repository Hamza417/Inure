package android.hardware.display;

import android.hardware.display.DisplayManager.DisplayListener;
import android.os.Build;
import android.os.Handler;
import android.view.DisplayInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class DisplayManagerGlobal {
    
    /**
     * Gets an instance of the display manager global singleton.
     *
     * @return The display manager instance, may be null early in system startup
     * before the display manager has been fully initialized.
     */
    public static DisplayManagerGlobal getInstance() {
        throw new RuntimeException();
    }
    
    /**
     * Get information about a particular logical display.
     *
     * @param displayId The logical display id.
     * @return Information about the specified display, or null if it does not exist.
     * This object belongs to an internal cache and should be treated as if it were immutable.
     */
    public DisplayInfo getDisplayInfo(int displayId) {
        throw new RuntimeException();
    }
    
    /**
     * Register a listener for display-related changes.
     *
     * @param listener The listener that will be called when display changes occur.
     * @param handler  Handler for the thread that will be receiving the callbacks. May be null.
     *                 If null, listener will use the handler for the current thread, and if still null,
     *                 the handler for the main thread.
     *                 If that is still null, a runtime exception will be thrown.
     */
    @RequiresApi (api = Build.VERSION_CODES.S)
    public void registerDisplayListener(@NonNull DisplayListener listener,
            @Nullable Handler handler, long eventsMask) {
        throw new RuntimeException();
    }
    
    public void registerDisplayListener(@NonNull DisplayListener listener,
            @Nullable Handler handler) {
        throw new RuntimeException();
    }
    
    public void unregisterDisplayListener(DisplayListener listener) {
        throw new RuntimeException();
    }
}
