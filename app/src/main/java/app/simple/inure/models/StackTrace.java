package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import app.simple.inure.crash.Utils;

@Entity (tableName = "stacktrace")
public class StackTrace implements Parcelable {
    
    @ColumnInfo (name = "trace")
    private String trace;
    
    @ColumnInfo (name = "message")
    private String message;
    
    @ColumnInfo (name = "cause")
    private String cause;
    
    @PrimaryKey
    @ColumnInfo (name = "timestamp")
    private Long timestamp;
    
    public StackTrace(String trace, String message, String cause, Long timestamp) {
        this.trace = trace;
        this.message = message;
        this.cause = cause;
        this.timestamp = timestamp;
    }
    
    @Ignore
    public StackTrace(Throwable throwable) {
        this.trace = throwable.toString();
        this.message = Utils.getCause(throwable).getMessage();
        this.cause = Utils.getCause(throwable).toString();
        this.timestamp = System.currentTimeMillis();
    }
    
    @Ignore
    public StackTrace() {
    }
    
    protected StackTrace(Parcel in) {
        trace = in.readString();
        message = in.readString();
        cause = in.readString();
        if (in.readByte() == 0) {
            timestamp = null;
        } else {
            timestamp = in.readLong();
        }
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trace);
        dest.writeString(message);
        dest.writeString(cause);
        if (timestamp == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(timestamp);
        }
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <StackTrace> CREATOR = new Creator <StackTrace>() {
        @Override
        public StackTrace createFromParcel(Parcel in) {
            return new StackTrace(in);
        }
        
        @Override
        public StackTrace[] newArray(int size) {
            return new StackTrace[size];
        }
    };
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getCause() {
        return cause;
    }
    
    public void setCause(String cause) {
        this.cause = cause;
    }
    
    public String getTrace() {
        return trace;
    }
    
    public void setTrace(String trace) {
        this.trace = trace;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
