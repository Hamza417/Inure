package app.simple.inure.apk.structure.string;

public class StringPool {
    private final String[] pool;
    
    public StringPool(int poolSize) {
        pool = new String[poolSize];
    }
    
    public String get(int idx) {
        return pool[idx];
    }
    
    public void set(int idx, String value) {
        pool[idx] = value;
    }
}