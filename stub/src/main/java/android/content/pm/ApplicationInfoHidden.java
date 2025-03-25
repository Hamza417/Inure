package android.content.pm;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class ApplicationInfoHidden {
    
    /**
     * If {@code true} this app requested to run in the legacy storage mode.
     */
    @RequiresApi (Build.VERSION_CODES.Q)
    public boolean hasRequestedLegacyExternalStorage() {
        throw new RuntimeException("Stub!");
    }
}
