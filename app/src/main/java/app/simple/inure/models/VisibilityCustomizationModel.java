package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class VisibilityCustomizationModel implements Parcelable {
    
    public static final Creator <VisibilityCustomizationModel> CREATOR = new Creator <VisibilityCustomizationModel>() {
        @Override
        public VisibilityCustomizationModel createFromParcel(Parcel in) {
            return new VisibilityCustomizationModel(in);
        }
        
        @Override
        public VisibilityCustomizationModel[] newArray(int size) {
            return new VisibilityCustomizationModel[size];
        }
    };
    
    @StringRes
    private int title;
    
    @DrawableRes
    private int icon;
    
    private String key;
    
    public VisibilityCustomizationModel(int title, int icon, String key) {
        this.title = title;
        this.icon = icon;
        this.key = key;
    }
    
    protected VisibilityCustomizationModel(Parcel in) {
        title = in.readInt();
        icon = in.readInt();
        key = in.readString();
    }
    
    public static Creator <VisibilityCustomizationModel> getCREATOR() {
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
    
        public VisibilityCustomizationModel build() {
            return new VisibilityCustomizationModel(title, icon, key);
        }
    }
}
