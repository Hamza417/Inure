package com.android.internal.app;

import android.app.AppOpsManagerHidden;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallback;
import android.os.RemoteException;

import java.util.List;

import androidx.annotation.RequiresApi;

public interface IAppOpsService extends IInterface {
    
    void resetAllModes(int userId, String packageName)
            throws RemoteException;
    
    void setMode(int code, int uid, String packageName, int mode)
            throws RemoteException;
    
    void setUidMode(int code, int uid, int mode)
            throws RemoteException;
    
    List <AppOpsManagerHidden.PackageOps> getOpsForPackage(int uid, String packageName, int[] ops)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.O)
    List <AppOpsManagerHidden.PackageOps> getUidOps(int uid, int[] ops)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.P)
    void startWatchingActive(int[] ops, IAppOpsActiveCallback callback)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.P)
    void stopWatchingActive(IAppOpsActiveCallback callback)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.P)
    boolean isOperationActive(int code, int uid, String packageName)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.Q)
    void startWatchingNoted(int[] ops, IAppOpsNotedCallback callback)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.Q)
    void stopWatchingNoted(IAppOpsNotedCallback callback)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.Q)
    void getHistoricalOps(int uid, String packageName, List <String> ops, long beginTimeMillis,
            long endTimeMillis, int flags, RemoteCallback callback)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.R)
    void getHistoricalOps(int uid, String packageName, String attributionTag, List <String> ops, int filter,
            long beginTimeMillis, long endTimeMillis, int flags, RemoteCallback callback)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.S)
    void getHistoricalOps(int uid, String packageName, String attributionTag, List <String> ops,
            int historyFlags, int filter, long beginTimeMillis, long endTimeMillis, int flags,
            RemoteCallback callback)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.Q)
    void getHistoricalOpsFromDiskRaw(int uid, String packageName, List <String> ops, long beginTimeMillis,
            long endTimeMillis, int flags, RemoteCallback callback)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.R)
    void getHistoricalOpsFromDiskRaw(int uid, String packageName, String attributionTag, List <String> ops, int filter,
            long beginTimeMillis, long endTimeMillis, int flags, RemoteCallback callback)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.S)
    void getHistoricalOpsFromDiskRaw(int uid, String packageName, String attributionTag,
            List <String> ops, int historyFlags, int filter, long beginTimeMillis,
            long endTimeMillis, int flags, RemoteCallback callback)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.Q)
    void setHistoryParameters(int mode, long baseSnapshotInterval, int compressionStep)
            throws RemoteException;
    
    @RequiresApi (Build.VERSION_CODES.Q)
    void resetHistoryParameters()
            throws RemoteException;
    
    abstract class Stub implements IAppOpsService {
        
        public static IAppOpsService asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
