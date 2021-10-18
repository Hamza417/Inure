package app.simple.inure.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

import app.simple.inure.R;

public class StatusBarHeight {
    /**
     * Get status bar height using window object
     *
     * @param window instance of the activity
     * @return int
     */
    public static int getStatusBarHeight(Window window) {
        Rect rectangle = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top - window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }
    
    /**
     * Get status bar height using system framework resources
     *
     * @param resources of the android system package
     * @return int
     */
    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    /**
     * Get navigation bar height using system framework resources
     *
     * @param resources of the android system package
     * @return int
     */
    public static int getNavigationBarHeight(Resources resources) {
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            if (isEdgeToEdgeEnabled(resources) == 2) {
                return 0;
            }
            else {
                resources.getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }
    
    /**
     * Get tool bar height using context resources
     *
     * @return int
     * <p>
     * Marked deprecated because this app does not plan
     * on using app bar any day
     */
    @Deprecated
    public static int getToolBarHeight(Context context) {
        // Calculate ActionBar height
        int i = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            i = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        
        return i;
    }
    
    /**
     * Checks if the current device has gesture mode turned on
     *
     * @param resources of the current context environment
     * @return 0 : Navigation is displaying with 3 buttons
     * 1 : Navigation is displaying with 2 button(Android P navigation mode)
     * 2 : Full screen gesture(Gesture on android Q)
     */
    public static int isEdgeToEdgeEnabled(Resources resources) {
        int resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
        if (resourceId > 0) {
            return resources.getInteger(resourceId);
        }
        return 0;
    }
    
    public static int getRotation(Context context) {
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            return Configuration.ORIENTATION_PORTRAIT;
        }
        
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            return Configuration.ORIENTATION_LANDSCAPE;
        }
        
        return -1;
    }
    
    public static boolean isLandscape(Context context) {
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            return false;
        }
        
        return rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270;
    }
}
