package android.view;

import android.os.Build;

import androidx.annotation.RequiresApi;
import dev.rikka.tools.refine.RefineAs;

@RefineAs (WindowManager.class)
public interface WindowManagerHidden {
    
    class LayoutParams {
        
        @RequiresApi (Build.VERSION_CODES.O)
        public static int PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY;
        
        public int privateFlags;
    }
}
