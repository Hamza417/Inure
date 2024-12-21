package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class DexClass implements Parcelable {
    
    private String className;
    private boolean isTracker;
    private String trackerSignature;
    
    public DexClass(String className, boolean isTracker, String trackerSignature) {
        this.className = className;
        this.isTracker = isTracker;
        this.trackerSignature = trackerSignature;
    }
    
    public DexClass() {
    }
    
    public DexClass(String className) {
        this.className = className;
    }
    
    protected DexClass(Parcel in) {
        className = in.readString();
        isTracker = in.readByte() != 0;
        trackerSignature = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(className);
        dest.writeByte((byte) (isTracker ? 1 : 0));
        dest.writeString(trackerSignature);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <DexClass> CREATOR = new Creator <DexClass>() {
        @Override
        public DexClass createFromParcel(Parcel in) {
            return new DexClass(in);
        }
        
        @Override
        public DexClass[] newArray(int size) {
            return new DexClass[size];
        }
    };
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public boolean isTracker() {
        return isTracker;
    }
    
    public void setTracker(boolean tracker) {
        isTracker = tracker;
    }
    
    public String getTrackerSignature() {
        return trackerSignature;
    }
    
    public void setTrackerSignature(String trackerSignature) {
        this.trackerSignature = trackerSignature;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "DexClass{" +
                "className='" + className + '\'' +
                ", isTracker=" + isTracker +
                ", trackerSignature='" + trackerSignature + '\'' +
                '}';
    }
}
