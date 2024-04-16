package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Extra implements Parcelable {
    
    private String path;
    private String name;
    private long size;
    
    public Extra(String path, String name, long size) {
        this.path = path;
        this.name = name;
        this.size = size;
    }
    
    public Extra() {
    }
    
    protected Extra(Parcel in) {
        path = in.readString();
        name = in.readString();
        size = in.readLong();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(name);
        dest.writeLong(size);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <Extra> CREATOR = new Creator <Extra>() {
        @Override
        public Extra createFromParcel(Parcel in) {
            return new Extra(in);
        }
        
        @Override
        public Extra[] newArray(int size) {
            return new Extra[size];
        }
    };
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
}
