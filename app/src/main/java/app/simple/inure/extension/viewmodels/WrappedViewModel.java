package app.simple.inure.extension.viewmodels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import app.simple.inure.util.ContextUtils;

public class WrappedViewModel extends AndroidViewModel {
    
    public WrappedViewModel(@NonNull Application application) {
        super(application);
    }
    
    public final Context getContext() {
        return ContextUtils.Companion.updateLocale(getApplication().getApplicationContext(), "en");
    }
    
    public final String getString(int id) {
        return getContext().getString(id);
    }
}
