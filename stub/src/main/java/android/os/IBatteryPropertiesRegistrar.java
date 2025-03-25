package android.os;

import androidx.annotation.RequiresApi;

public interface IBatteryPropertiesRegistrar extends IInterface {
    
    @RequiresApi (Build.VERSION_CODES.O)
    int getProperty(int id, BatteryProperty prop)
            throws RemoteException;
    
    void scheduleUpdate()
            throws RemoteException;
    
    abstract class Stub extends Binder implements IBatteryPropertiesRegistrar {
        
        public static IBatteryPropertiesRegistrar asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}

