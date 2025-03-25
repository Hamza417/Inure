package android.hardware.display;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.view.DisplayInfo;

public interface IDisplayManager extends IInterface {
    
    DisplayInfo getDisplayInfo(int displayId)
            throws RemoteException;
    
    void registerCallback(IDisplayManagerCallback callback)
            throws RemoteException;
    
    abstract class Stub extends Binder implements IDisplayManager {
        
        public static IDisplayManager asInterface(IBinder binder) {
            throw new RuntimeException();
        }
        
        @Override
        public IBinder asBinder() {
            throw new RuntimeException();
        }
    }
}
