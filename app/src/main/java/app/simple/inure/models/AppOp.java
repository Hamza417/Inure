package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import app.simple.inure.enums.AppOpMode;
import app.simple.inure.enums.AppOpScope;

public class AppOp implements Parcelable {
    private String permission;
    private String id;
    private AppOpMode mode;
    private String time;
    private String duration;
    private String rejectTime;
    private AppOpScope scope;
    
    public AppOp(String permission, String id, AppOpMode mode, String time, String duration, String rejectTime) {
        this.permission = permission;
        this.id = id;
        this.mode = mode;
        this.time = time;
        this.duration = duration;
        this.rejectTime = rejectTime;
    }
    
    public AppOp() {
    }
    
    protected AppOp(Parcel in) {
        permission = in.readString();
        id = in.readString();
        mode = AppOpMode.valueOf(in.readString());
        scope = AppOpScope.valueOf(in.readString());
        time = in.readString();
        duration = in.readString();
        rejectTime = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(permission);
        dest.writeString(id);
        dest.writeString(mode.name());
        dest.writeString(time);
        dest.writeString(duration);
        dest.writeString(rejectTime);
        dest.writeString(scope.name());
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <AppOp> CREATOR = new Creator <AppOp>() {
        @Override
        public AppOp createFromParcel(Parcel in) {
            return new AppOp(in);
        }
        
        @Override
        public AppOp[] newArray(int size) {
            return new AppOp[size];
        }
    };
    
    public String getPermission() {
        return permission;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public AppOpMode getMode() {
        return mode;
    }
    
    public void setMode(AppOpMode mode) {
        this.mode = mode;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public String getRejectTime() {
        return rejectTime;
    }
    
    public void setRejectTime(String rejectTime) {
        this.rejectTime = rejectTime;
    }
    
    public AppOpScope getScope() {
        return scope;
    }
    
    public void setScope(AppOpScope scope) {
        this.scope = scope;
    }
    
    public static String getModeString(AppOpMode mode) {
        return switch (mode) {
            case ALLOW ->
                    "allow";
            case DENY ->
                    "deny";
            case IGNORE ->
                    "ignore";
            case ASK ->
                    "ask";
            case DEFAULT ->
                    "default";
            case FOREGROUND ->
                    "foreground";
            default ->
                    "unknown";
        };
    }
    
    public static AppOpMode getModeFromString(String mode) {
        return switch (mode) {
            case "allow" ->
                    AppOpMode.ALLOW;
            case "deny" ->
                    AppOpMode.DENY;
            case "ignore" ->
                    AppOpMode.IGNORE;
            case "ask" ->
                    AppOpMode.ASK;
            case "foreground" ->
                    AppOpMode.FOREGROUND;
            default ->
                    AppOpMode.DEFAULT;
        };
    }
}
