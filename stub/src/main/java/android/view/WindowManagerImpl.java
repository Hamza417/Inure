package android.view;

import android.os.IBinder;

public abstract class WindowManagerImpl implements WindowManager {
    
    /**
     * Sets the window token to assign when none is specified by the client or
     * available from the parent window.
     *
     * @param token The default token to assign.
     */
    public void setDefaultToken(IBinder token) {
        throw new RuntimeException("STUB");
    }
}
