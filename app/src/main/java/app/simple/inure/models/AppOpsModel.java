package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class AppOpsModel implements Parcelable {
    private String title;
    private String description;
    private boolean isEnabled;
    
    public AppOpsModel(String title, String description, boolean enabled) {
        this.title = title;
        this.description = description;
        this.isEnabled = enabled;
    }
    
    protected AppOpsModel(Parcel in) {
        title = in.readString();
        description = in.readString();
        isEnabled = in.readByte() != 0;
    }
    
    public static final Creator <AppOpsModel> CREATOR = new Creator <AppOpsModel>() {
        @Override
        public AppOpsModel createFromParcel(Parcel in) {
            return new AppOpsModel(in);
        }
        
        @Override
        public AppOpsModel[] newArray(int size) {
            return new AppOpsModel[size];
        }
    };
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setTitle(String mTitle) {
        this.title = mTitle;
    }
    
    public void setDescription(String mDescription) {
        this.description = mDescription;
    }
    
    public void setEnabled(boolean mEnabled) {
        this.isEnabled = mEnabled;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeByte((byte) (isEnabled ? 1 : 0));
    }
}