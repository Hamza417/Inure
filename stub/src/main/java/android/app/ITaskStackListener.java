package android.app;

import android.os.Binder;

public interface ITaskStackListener {
    
    /**
     * Called whenever there are changes to the state of tasks in a stack.
     */
    void onTaskStackChanged();
    
    abstract class Stub extends Binder implements ITaskStackListener {
    
    }
}
