package com.android.internal.policy;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IKeyguardLockedStateListener extends IInterface {
    
    void onKeyguardLockedStateChanged(boolean isKeyguardLocked) throws RemoteException;
    
    abstract class Stub extends Binder implements IKeyguardLockedStateListener {
        
        public static IKeyguardLockedStateListener asInterface(IBinder binder) {
            throw new RuntimeException();
        }
        
        @Override
        public IBinder asBinder() {
            throw new RuntimeException();
        }
    }
}
