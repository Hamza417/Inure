package com.android.internal.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IAppOpsNotedCallback extends IInterface {
    
    void opNoted(int op, int uid, String packageName, int mode) throws RemoteException;
    
    void opNoted(int op, int uid, String packageName, String attributionTag, int flags, int mode) throws RemoteException;
    
    abstract class Stub extends Binder implements IAppOpsNotedCallback {
        
        public Stub() {
            throw new RuntimeException("STUB");
        }
        
        public static IAppOpsNotedCallback asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
        
        @Override
        public IBinder asBinder() {
            throw new RuntimeException("STUB");
        }
    }
}
