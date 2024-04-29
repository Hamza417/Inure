package app.simple.inure.models;

import androidx.annotation.NonNull;

public class ShizukuInstall {
    
    private int status;
    private String message;
    
    public ShizukuInstall(int status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @NonNull
    public String toString() {
        return "ShizukuInstall{status=" + this.status + ", message=" + this.message + "}";
    }
    
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ShizukuInstall shizukuInstall)) {
            return false;
        }
        return shizukuInstall.getStatus() == this.status && shizukuInstall.getMessage().equals(this.message);
    }
    
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.status;
        result = 31 * result + (this.message == null ? 0 : this.message.hashCode());
        return result;
    }
}
