package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "stacktrace")
public class StackTrace implements Parcelable {
    
    @ColumnInfo (name = "trace")
    private String trace;
    
    @PrimaryKey
    @ColumnInfo (name = "timestamp")
    private Long timestamp;
    
    public StackTrace(String trace, Long timestamp) {
        this.trace = trace;
        this.timestamp = timestamp;
    }
    
    public StackTrace() {
    }
    
    protected StackTrace(Parcel in) {
        trace = in.readString();
        if (in.readByte() == 0) {
            timestamp = null;
        } else {
            timestamp = in.readLong();
        }
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trace);
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
    
    public static final Creator <StackTrace> CREATOR = new Creator <>() {
        @Override
        public StackTrace createFromParcel(Parcel in) {
            return new StackTrace(in);
        }
        
        @Override
        public StackTrace[] newArray(int size) {
            return new StackTrace[size];
        }
    };
    
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
