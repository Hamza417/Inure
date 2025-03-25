package android.app;

import android.os.Build;

import androidx.annotation.RequiresApi;
import dev.rikka.tools.refine.RefineAs;

@RefineAs (ActivityManager.class)
public class ActivityManagerHidden {
    
    /**
     * Flag for registerUidObserver: report changes in process state.
     * AOSP value: 1<<0
     */
    @RequiresApi (Build.VERSION_CODES.N)
    public static int UID_OBSERVER_PROCSTATE;
    
    /**
     * Flag for registerUidObserver: report uid gone.
     * AOSP value: 1<<1
     */
    @RequiresApi (Build.VERSION_CODES.N)
    public static int UID_OBSERVER_GONE;
    
    /**
     * Flag for registerUidObserver: report uid has become idle.
     * AOSP value: 1<<2
     */
    @RequiresApi (Build.VERSION_CODES.N)
    public static int UID_OBSERVER_IDLE;
    
    /**
     * Flag for registerUidObserver: report uid has become active.
     * AOSP value: 1<<3
     */
    @RequiresApi (Build.VERSION_CODES.N)
    public static int UID_OBSERVER_ACTIVE;
    
    /**
     * Flag for registerUidObserver: report uid cached state has changed.
     * AOSP value: 1<<4
     */
    @RequiresApi (Build.VERSION_CODES.O_MR1)
    public static int UID_OBSERVER_CACHED;
    
    /**
     * Flag for registerUidObserver: report uid capability has changed.
     * AOSP value: 1<<5
     */
    @RequiresApi (Build.VERSION_CODES.S)
    public static int UID_OBSERVER_CAPABILITY;
    
    @RequiresApi (24)
    public static int FLAG_AND_UNLOCKED;
    
    public static int PROCESS_STATE_UNKNOWN;
    
    public static int PROCESS_STATE_TOP;
    
    public static class RunningAppProcessInfo {
        
        public static int procStateToImportance(int procState) {
            throw new RuntimeException("STUB");
        }
    }
    
    public static boolean isHighEndGfx() {
        throw new RuntimeException("STUB");
    }
}
