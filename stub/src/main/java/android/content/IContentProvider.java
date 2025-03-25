package android.content;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ICancellationSignal;
import android.os.IInterface;
import android.os.RemoteException;

import java.io.FileNotFoundException;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public interface IContentProvider extends IInterface {
    
    Cursor query(String callingPkg, Uri url, String[] projection, String selection,
            String[] selectionArgs, String sortOrder, ICancellationSignal cancellationSignal)
            throws RemoteException;
    
    @RequiresApi (26)
    Cursor query(String callingPkg, Uri url, @Nullable String[] projection,
            @Nullable Bundle queryArgs, @Nullable ICancellationSignal cancellationSignal)
            throws RemoteException;
    
    @RequiresApi (30)
    Cursor query(String callingPkg, String featureId, Uri url, @Nullable String[] projection,
            @Nullable Bundle queryArgs, @Nullable ICancellationSignal cancellationSignal)
            throws RemoteException;
    
    @RequiresApi (31)
    Cursor query(AttributionSource source, Uri url, String[] projection, Bundle queryArgs,
            ICancellationSignal cancellationSignal)
            throws RemoteException;
    
    AssetFileDescriptor openAssetFile(
            String callingPkg, Uri url, String mode, ICancellationSignal signal)
            throws RemoteException, FileNotFoundException;
    
    @RequiresApi (30)
    AssetFileDescriptor openAssetFile(
            String callingPkg, String featureId, Uri url, String mode, ICancellationSignal signal)
            throws RemoteException, FileNotFoundException;
    
    @RequiresApi (31)
    AssetFileDescriptor openAssetFile(AttributionSource source, Uri uri, String mode, ICancellationSignal signal)
            throws RemoteException, FileNotFoundException;
    
    Bundle call(String callingPkg, String method, @Nullable String arg, @Nullable Bundle extras)
            throws RemoteException;
    
    @RequiresApi (29)
    Bundle call(String callingPkg, String authority, String method, @Nullable String arg, @Nullable Bundle extras)
            throws RemoteException;
    
    @RequiresApi (30)
    Bundle call(String callingPkg, String featureId, String authority, String method, @Nullable String arg, @Nullable Bundle extras)
            throws RemoteException;
    
    @RequiresApi (31)
    Bundle call(AttributionSource attributionSource, String authority, String method, @Nullable String arg, @Nullable Bundle extras)
            throws RemoteException;
}
