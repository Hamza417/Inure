package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.StringRes;

public class Shortcut implements Parcelable {
    private int icon;
    private String id;
    private String action;
    
    @StringRes
    private int name;
    
    public Shortcut(int icon, String id, String action, int name) {
        this.icon = icon;
        this.id = id;
        this.action = action;
        this.name = name;
    }
    
    protected Shortcut(Parcel in) {
        icon = in.readInt();
        id = in.readString();
        action = in.readString();
        name = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(icon);
        dest.writeString(id);
        dest.writeString(action);
        dest.writeInt(name);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <Shortcut> CREATOR = new Creator <>() {
        @Override
        public Shortcut createFromParcel(Parcel in) {
            return new Shortcut(in);
        }
        
        @Override
        public Shortcut[] newArray(int size) {
            return new Shortcut[size];
        }
    };
    
    public int getIcon() {
        return icon;
    }
    
    public void setIcon(int icon) {
        this.icon = icon;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public int getName() {
        return name;
    }
    
    public void setName(int name) {
        this.name = name;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
}
