package android.content;

import android.os.Binder;
import android.os.IBinder;

public abstract class ContentProviderNative extends Binder implements IContentProvider {
    
    // removed from 30
    public static IContentProvider asInterface(IBinder binder) {
        throw new RuntimeException("STUB");
    }
    
    public abstract String getProviderName();
    
    @Override
    public IBinder asBinder() {
        throw new RuntimeException("STUB");
    }
}

class ContentProviderProxy {
    
    public ContentProviderProxy(IBinder remote) {
        throw new RuntimeException("STUB");
    }
}
