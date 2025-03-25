package android.hardware.display;

import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;
import dev.rikka.tools.refine.RefineAs;

@RefineAs (DisplayManager.class)
public class DisplayManagerHidden {
    
    /**
     * Event type for when a new display is added.
     */
    @RequiresApi (api = Build.VERSION_CODES.S)
    public static long EVENT_FLAG_DISPLAY_ADDED;
    
    /**
     * Event type for when a display is removed.
     */
    @RequiresApi (api = Build.VERSION_CODES.S)
    public static long EVENT_FLAG_DISPLAY_REMOVED;
    
    /**
     * Event type for when a display is changed.
     *
     * @see #registerDisplayListener(DisplayManager.DisplayListener, Handler, long)
     */
    @RequiresApi (api = Build.VERSION_CODES.S)
    public static long EVENT_FLAG_DISPLAY_CHANGED;
    
    /**
     * Event flag to register for a display's brightness changes. This notification is sent
     * through the {@link DisplayManager.DisplayListener#onDisplayChanged} callback method. New brightness
     * values can be retrieved via {@link android.view.Display#getBrightnessInfo()}.
     */
    @RequiresApi (api = Build.VERSION_CODES.S)
    public static long EVENT_FLAG_DISPLAY_BRIGHTNESS;
}
