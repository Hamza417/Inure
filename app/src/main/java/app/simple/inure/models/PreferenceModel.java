package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

public class PreferenceModel implements Parcelable {
    private int icon;
    private int title;
    private int description;
    private int type;
    private int category;
    private int panel;
    
    public PreferenceModel(int icon, int title, int description, int type, int category, int panel) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.panel = panel;
    }
    
    protected PreferenceModel(Parcel in) {
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
    
    public static final Creator <PreferenceModel> CREATOR = new Creator <PreferenceModel>() {
        @Override
        public PreferenceModel createFromParcel(Parcel in) {
            return new PreferenceModel(in);
        }
        
        @Override
        public PreferenceModel[] newArray(int size) {
            return new PreferenceModel[size];
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
    
    public static Creator <PreferenceModel> getCREATOR() {
        return CREATOR;
    }
}