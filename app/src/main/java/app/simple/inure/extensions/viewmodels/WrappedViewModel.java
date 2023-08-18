package app.simple.inure.extensions.viewmodels;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import app.simple.inure.database.instances.StackTraceDatabase;
import app.simple.inure.extensions.livedata.ErrorLiveData;
import app.simple.inure.preferences.ConfigurationPreferences;
import app.simple.inure.util.ContextUtils;

public class WrappedViewModel extends AndroidViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    public final ErrorLiveData error = new ErrorLiveData();
    public final MutableLiveData <String> warning = new MutableLiveData <>();
    public final MutableLiveData <Integer> notFound = new MutableLiveData <>();
    protected final Handler handler = new Handler(Looper.getMainLooper());
    
    public WrappedViewModel(@NonNull Application application) {
        super(application);
        app.simple.inure.preferences.SharedPreferences.INSTANCE.registerSharedPreferenceChangeListener(this);
    }
    
    public final Context getContext() {
        return ContextUtils.Companion.updateLocale(applicationContext(), ConfigurationPreferences.INSTANCE.getAppLanguage());
    }
    
    public LiveData <Throwable> getError() {
        return error;
    }
    
    public LiveData <String> getWarning() {
        return warning;
    }
    
    public LiveData <Integer> getNotFound() {
        return notFound;
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
    
    protected void postWarning(String string) {
        warning.postValue(string);
    }
    
    protected void postError(Throwable throwable) {
        error.postError(throwable, getApplication());
    }
    
    public void onAppUninstalled(String packageName) {
    
    }
    
    public void cleanErrorStack() {
        error.postValue(null);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        try {
            StackTraceDatabase.Companion.getInstance().close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    
        app.simple.inure.preferences.SharedPreferences.INSTANCE.unregisterSharedPreferenceChangeListener(this);
        handler.removeCallbacksAndMessages(null);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
    
    }
    
    protected void postDelayed(long delay, Runnable runnable) {
        handler.postDelayed(runnable, delay);
    }
    
    protected void removeCallbacks() {
        handler.removeCallbacksAndMessages(null);
    }
}