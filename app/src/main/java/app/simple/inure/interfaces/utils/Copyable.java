package app.simple.inure.interfaces.utils;

public interface Copyable <T> {
    T copy();
    
    T createForCopy();
    
    void copyTo(T dest);
}