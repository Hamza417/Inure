package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * This model is based on <a href="https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/content/pm/UserInfo.java">UserInfo</a>
 * class in Android Open Source Project and loosely follows its toString() method.
 *
 * <p>
 * return "UserInfo{" + id + ":" + name + ":" + Integer.toHexString(flags) + "}";
 * <p>
 */
public class User implements Parcelable {
    
    public static final Creator <User> CREATOR = new Creator <User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }
        
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private int id;
    private String name;
    private String hexFlags;
    
    public User(int id, String name, String hexFlags) {
        this.id = id;
        this.name = name;
        this.hexFlags = hexFlags;
    }
    
    public User() {
    }
    
    protected User(Parcel in) {
        id = in.readInt();
        name = in.readString();
        hexFlags = in.readString();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getHexFlags() {
        return hexFlags;
    }
    
    public void setHexFlags(String hexFlags) {
        this.hexFlags = hexFlags;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(hexFlags);
    }
}
