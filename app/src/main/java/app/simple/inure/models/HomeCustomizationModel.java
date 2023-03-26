package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class HomeCustomizationModel implements Parcelable {
    
    public static final Creator <HomeCustomizationModel> CREATOR = new Creator <HomeCustomizationModel>() {
        @Override
        public HomeCustomizationModel createFromParcel(Parcel in) {
            return new HomeCustomizationModel(in);
        }
        
        @Override
        public HomeCustomizationModel[] newArray(int size) {
            return new HomeCustomizationModel[size];
        }
    };
    private int title;
    private int icon;
    private String key;
    
    public HomeCustomizationModel(int title, int icon, String key) {
        this.title = title;
        this.icon = icon;
        this.key = key;
    }
    
    protected HomeCustomizationModel(Parcel in) {
        title = in.readInt();
        icon = in.readInt();
        key = in.readString();
    }
    
    public static Creator <HomeCustomizationModel> getCREATOR() {
        return CREATOR;
    }
    
    public int getTitle() {
        return title;
    }
    
    public void setTitle(int title) {
        this.title = title;
    }
    
    public int getIcon() {
        return icon;
    }
    
    public void setIcon(int icon) {
        this.icon = icon;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "HomeCustomizationModel{" +
                "title=" + title +
                ", icon=" + icon +
                ", key='" + key + '\'' +
                '}';
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(title);
        dest.writeInt(icon);
        dest.writeString(key);
    }
    
    public static class Builder {
        
        private int title;
        private int icon;
        private String key;
        
        public Builder setTitle(int title) {
            this.title = title;
            return this;
        }
        
        public Builder setIcon(int icon) {
            this.icon = icon;
            return this;
        }
        
        public Builder setKey(String key) {
            this.key = key;
            return this;
        }
        
        public HomeCustomizationModel build() {
            return new HomeCustomizationModel(title, icon, key);
        }
    }
}
