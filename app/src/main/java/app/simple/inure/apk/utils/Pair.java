package app.simple.inure.apk.utils;

public class Pair <K, V> {
    private K left;
    private V right;
    
    public Pair() {
    }
    
    public Pair(K left, V right) {
        this.left = left;
        this.right = right;
    }
    
    public K getLeft() {
        return left;
    }
    
    public void setLeft(K left) {
        this.left = left;
    }
    
    public V getRight() {
        return right;
    }
    
    public void setRight(V right) {
        this.right = right;
    }
}