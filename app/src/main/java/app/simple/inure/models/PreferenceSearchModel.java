package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

public class PreferenceSearchModel implements Parcelable {
    private int icon;
    private int title;
    private int description;
    private int type;
    private int category;
    private int panel;
    
    public PreferenceSearchModel(int icon, int title, int description, int type, int category, int panel) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.panel = panel;
    }
    
    protected PreferenceSearchModel(Parcel in) {
        icon = in.readInt();
        title = in.readInt();
        description = in.readInt();
        type = in.readInt();
        category = in.readInt();
        panel = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(icon);
        dest.writeInt(title);
        dest.writeInt(description);
        dest.writeInt(type);
        dest.writeInt(category);
        dest.writeInt(panel);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <PreferenceSearchModel> CREATOR = new Creator <PreferenceSearchModel>() {
        @Override
        public PreferenceSearchModel createFromParcel(Parcel in) {
            return new PreferenceSearchModel(in);
        }
        
        @Override
        public PreferenceSearchModel[] newArray(int size) {
            return new PreferenceSearchModel[size];
        }
    };
    
    public int getIcon() {
        return icon;
    }
    
    public void setIcon(int icon) {
        this.icon = icon;
    }
    
    public int getTitle() {
        return title;
    }
    
    public void setTitle(int title) {
        this.title = title;
    }
    
    public int getDescription() {
        return description;
    }
    
    public void setDescription(int description) {
        this.description = description;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public int getCategory() {
        return category;
    }
    
    public void setCategory(int category) {
        this.category = category;
    }
    
    public int getPanel() {
        return panel;
    }
    
    public void setPanel(int panel) {
        this.panel = panel;
    }
    
    public static Creator <PreferenceSearchModel> getCREATOR() {
        return CREATOR;
    }
}