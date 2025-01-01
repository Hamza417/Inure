package app.simple.inure.models;

public class Tuple <T, K> {
    
    private T one;
    private K two;
    
    public Tuple(T param1, K param2) {
        one = param1;
        two = param2;
    }
    
    public T getFirst() {
        return one;
    }
    
    public K getSecond() {
        return two;
    }
    
    public void setFirst(T t) {
        one = t;
    }
    
    public void setSecond(K k) {
        two = k;
    }
    
    public int compareTo(Tuple tt) {
        int i = one.toString().toLowerCase().compareTo(tt.getFirst().toString().toLowerCase());
        if (i == 0) {
            return two.toString().toLowerCase().compareTo(tt.getSecond().toString().toLowerCase());
        }
        else if (i < 0) {
            return -1;
        }
        else {
            return 1;
        }
    }
}
