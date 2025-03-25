package android.os;

import android.content.pm.UserInfo;

import java.util.List;

import androidx.annotation.RequiresApi;

public interface IUserManager extends IInterface {
    
    @RequiresApi (26)
    boolean isUserUnlocked(int userId)
            throws RemoteException;
    
    @RequiresApi (26)
    boolean isUserRunning(int userId)
            throws RemoteException;
    
    List <UserInfo> getUsers(boolean excludeDying)
            throws RemoteException;
    
    @RequiresApi (30)
    List <UserInfo> getUsers(boolean excludePartial, boolean excludeDying, boolean excludePreCreated)
            throws RemoteException;
    
    UserInfo getUserInfo(int userId);
    
    abstract class Stub extends Binder implements IUserManager {
        
        public static IUserManager asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
