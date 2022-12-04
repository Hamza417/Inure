package app.simple.inure.extensions.viewmodels;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import app.simple.inure.database.instances.StackTraceDatabase;
import app.simple.inure.extensions.livedata.ErrorLiveData;
import app.simple.inure.preferences.ConfigurationPreferences;
import app.simple.inure.receivers.AppUninstalledBroadcastReceiver;
import app.simple.inure.util.ContextUtils;

public class WrappedViewModel extends AndroidViewModel {
    
    public final ErrorLiveData error = new ErrorLiveData();
    public final MutableLiveData <String> warning = new MutableLiveData <>();
    public final MutableLiveData <Integer> notFound = new MutableLiveData <>();
    
    private final AppUninstalledBroadcastReceiver appUninstallBroadcastReceiver = new AppUninstalledBroadcastReceiver();
    
    public WrappedViewModel(@NonNull Application application) {
        super(application);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        getApplication().getApplicationContext().registerReceiver(appUninstallBroadcastReceiver, intentFilter);
        appUninstallBroadcastReceiver.setAppUninstallCallbacks(packageName -> {
            onAppUninstalled(packageName);
            Log.d("AppUninstalled", packageName);
        });
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
    
    protected void onAppUninstalled(String packageName) {
    
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        try {
            StackTraceDatabase.Companion.getInstance().close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        
        try {
            getApplication().getApplicationContext().unregisterReceiver(appUninstallBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}