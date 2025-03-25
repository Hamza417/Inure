package android.app;

import android.graphics.Rect;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi (api = Build.VERSION_CODES.Q)
public class ActivityTaskManager {
    
    @RequiresApi (api = Build.VERSION_CODES.S)
    public static class RootTaskInfo extends TaskInfo {
        
        public Rect bounds;
        public int[] childTaskIds;
        public String[] childTaskNames;
        public Rect[] childTaskBounds;
        public int[] childTaskUserIds;
        public boolean visible;
        // Index of the stack in the display's stack list, can be used for comparison of stack order
        public int position;
    }
}
