package android.view;

import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import com.android.internal.policy.IKeyguardLockedStateListener;

import androidx.annotation.RequiresApi;

public interface IWindowManager extends IInterface {
    
    @RequiresApi (Build.VERSION_CODES.TIRAMISU)
    void addKeyguardLockedStateListener(IKeyguardLockedStateListener listener) throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.TIRAMISU)
    void removeKeyguardLockedStateListener(IKeyguardLockedStateListener listener) throws RemoteException;
    
    abstract class Stub extends Binder implements IWindowManager {
        
        public static IWindowManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
