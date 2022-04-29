package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ShortcutModel implements Parcelable {
    private int icon;
    private String id;
    private String action;
    private String name;
    
    public ShortcutModel(int icon, String id, String action, String name) {
        this.icon = icon;
        this.id = id;
        this.action = action;
        this.name = name;
    }
    
    protected ShortcutModel(Parcel in) {
        icon = in.readInt();
        id = in.readString();
        action = in.readString();
        name = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(icon);
        dest.writeString(id);
        dest.writeString(action);
        dest.writeString(name);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <ShortcutModel> CREATOR = new Creator <ShortcutModel>() {
        @Override
        public ShortcutModel createFromParcel(Parcel in) {
            return new ShortcutModel(in);
        }
        
        @Override
        public ShortcutModel[] newArray(int size) {
            return new ShortcutModel[size];
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
}
