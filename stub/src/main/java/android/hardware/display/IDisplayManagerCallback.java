package android.hardware.display;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IDisplayManagerCallback extends IInterface {
    
    void onDisplayEvent(int displayId, int event);
    
    abstract class Stub extends Binder implements IDisplayManagerCallback {
        
        public static IDisplayManager asInterface(IBinder binder) {
            throw new RuntimeException();
        }
        
        @Override
        public IBinder asBinder() {
            throw new RuntimeException();
        }
    }
}
