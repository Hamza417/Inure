package app.simple.inure.enums;

public enum Removal {
    ADVANCED("Advanced"),
    EXPERT("Expert"),
    RECOMMENDED("Recommended");
    
    private final String method;
    
    Removal(String method) {
        this.method = method;
    }
    
    public String getMethod() {
        return method;
    }
}
