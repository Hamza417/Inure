package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UnpackModel implements Parcelable {
    
    int type;
    String name;
    String path;
    String size;
    
    public UnpackModel() {
    
    }
    
    public UnpackModel(int type, String name, String path, String size) {
        this.type = type;
        this.name = name;
        this.path = path;
        this.size = size;
    }
    
    protected UnpackModel(Parcel in) {
        type = in.readInt();
        name = in.readString();
        path = in.readString();
        size = in.readString();
    }
    
    public static final Creator <UnpackModel> CREATOR = new Creator <>() {
        @Override
        public UnpackModel createFromParcel(Parcel in) {
            return new UnpackModel(in);
        }
        
        @Override
        public UnpackModel[] newArray(int size) {
            return new UnpackModel[size];
        }
    };
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(size);
    }
}
