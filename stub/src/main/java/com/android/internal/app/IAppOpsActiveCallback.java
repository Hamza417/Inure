package com.android.internal.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IAppOpsActiveCallback extends IInterface {
    
    void opActiveChanged(int op, int uid, String packageName, boolean active) throws RemoteException;
    
    void opActiveChanged(int op, int uid, String packageName, String attributionTag, boolean active, int attributionFlags, int attributionChainId) throws RemoteException;
    
    abstract class Stub extends Binder implements IAppOpsActiveCallback {
        
        public Stub() {
            throw new RuntimeException("STUB");
        }
        
        public static IAppOpsActiveCallback asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
        
        @Override
        public IBinder asBinder() {
            throw new RuntimeException("STUB");
        }
    }
}
