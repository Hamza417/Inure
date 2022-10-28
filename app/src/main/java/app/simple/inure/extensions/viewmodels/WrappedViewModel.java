package app.simple.inure.extensions.viewmodels;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import app.simple.inure.apk.utils.PackageUtils;
import app.simple.inure.database.instances.StackTraceDatabase;
import app.simple.inure.extensions.livedata.ErrorLiveData;
import app.simple.inure.preferences.ConfigurationPreferences;
import app.simple.inure.util.ContextUtils;

public class WrappedViewModel extends AndroidViewModel {
    
    public final ErrorLiveData error = new ErrorLiveData();
    public final MutableLiveData <String> warning = new MutableLiveData <>();
    public final MutableLiveData <Integer> notFound = new MutableLiveData <>();
    
    public WrappedViewModel(@NonNull Application application) {
        super(application);
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
    
    protected List <ApplicationInfo> getApps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getPackageManager().getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0));
        } else {
            //noinspection deprecation
            return getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        }
    }
    
    protected void postWarning(String string) {
        warning.postValue(string);
    }
    
    protected void postError(Throwable throwable) {
        error.postError(throwable, getApplication());
    }
    
    protected ArrayList <PackageInfo> getInstalledPackages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return (ArrayList <PackageInfo>) getPackageManager()
                    .getInstalledPackages(PackageManager
                            .PackageInfoFlags.of(PackageUtils.INSTANCE.getFlags()));
        } else {
            //noinspection deprecation
            return (ArrayList <PackageInfo>) getPackageManager().getInstalledPackages((int) PackageUtils.INSTANCE.getFlags());
        }
    }
    
    protected ApplicationInfo getApplicationInfo(String packageName) throws PackageManager.NameNotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getPackageManager().getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(PackageUtils.INSTANCE.getFlags()));
        } else {
            //noinspection deprecation
            return getPackageManager().getApplicationInfo(packageName, (int) PackageUtils.INSTANCE.getFlags());
        }
    }
    
    protected PackageInfo getPackageInfo(String packageName) throws PackageManager.NameNotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getPackageManager().getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageUtils.INSTANCE.getFlags()));
        } else {
            //noinspection deprecation
            return getPackageManager().getPackageInfo(packageName, (int) PackageUtils.INSTANCE.getFlags());
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        try {
            StackTraceDatabase.Companion.getInstance().close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}