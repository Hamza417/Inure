package app.simple.inure.enums;

import androidx.annotation.NonNull;

public enum AppOpMode {
    ALLOW("allow"),
    DENY("deny"),
    IGNORE("ignore"),
    ASK("ask"),
    DEFAULT("default"),
    FOREGROUND("foreground");
    
    private final String value;
    
    AppOpMode(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static AppOpMode fromString(String mode) {
        for (AppOpMode appOpMode : AppOpMode.values()) {
            if (appOpMode.value.equalsIgnoreCase(mode)) {
                return appOpMode;
            }
        }
        return DEFAULT;
    }
    
    @NonNull
    @Override
    public String toString() {
        return value;
    }
}
