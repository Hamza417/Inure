package app.simple.inure.interfaces.receivers;

public interface AppUninstallCallbacks {
    void onAppUninstalled(String packageName);
    
    void onAppInstalled(String packageName);
    
    void onAppUpdated(String packageName);
    
    void onAppReplaced(String packageName);
}
