package app.simple.inure.interfaces.parsers;

public interface FOSSCallbacks {
    void onFOSSAdded(String packageName);
    
    void onFOSSRemoved(String packageName);
}