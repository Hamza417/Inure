package app.simple.inure.enums;

import androidx.annotation.NonNull;

public enum AppOpScope {
    UID("uid"),
    PACKAGE("package");
    
    private final String value;
    
    AppOpScope(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static String getCommandFlag(AppOpScope scope) {
        if (scope == PACKAGE) {
            return ""; // No flag needed for package scope
        } else {
            return "--uid";
        }
    }
    
    @NonNull
    @Override
    public String toString() {
        return value;
    }
}
