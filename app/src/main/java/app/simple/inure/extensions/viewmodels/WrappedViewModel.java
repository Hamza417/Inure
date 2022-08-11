package app.simple.inure.extensions.viewmodels;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import app.simple.inure.preferences.ConfigurationPreferences;
import app.simple.inure.util.ContextUtils;

public class WrappedViewModel extends AndroidViewModel {
    
    public final MutableLiveData <String> error = new MutableLiveData <>();
    public final MutableLiveData <Integer> notFound = new MutableLiveData <>();
    
    public WrappedViewModel(@NonNull Application application) {
        super(application);
    }
    
    public final Context getContext() {
        return ContextUtils.Companion.updateLocale(applicationContext(), ConfigurationPreferences.INSTANCE.getAppLanguage());
    }
    
    public final Context applicationContext() {
        return getApplication().getApplicationContext();
    }
    
    public final String getString(int id) {
        return getContext().getString(id);
    }
    
    public final String getString(int resId, Object... formatArgs) {
        return getContext().getString(resId, formatArgs);
    }
    
    public final ContentResolver getContentResolver() {
        return getApplication().getContentResolver();
    }
    
    public final PackageManager getPackageManager() {
        return getContext().getPackageManager();
    }
    
}