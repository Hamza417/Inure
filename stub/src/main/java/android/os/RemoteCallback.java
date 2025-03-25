package android.os;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RemoteCallback {
    
    public interface OnResultListener {
        void onResult(@Nullable Bundle result);
    }
    
    public RemoteCallback(OnResultListener listener) {
        throw new RuntimeException();
    }
    
    public RemoteCallback(@NonNull OnResultListener listener, @Nullable Handler handler) {
        throw new RuntimeException();
    }
}
