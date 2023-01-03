package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TrackersMessageModel implements Parcelable {
    
    public static final Creator <TrackersMessageModel> CREATOR = new Creator <TrackersMessageModel>() {
        @Override
        public TrackersMessageModel createFromParcel(Parcel in) {
            return new TrackersMessageModel(in);
        }
        
        @Override
        public TrackersMessageModel[] newArray(int size) {
            return new TrackersMessageModel[size];
        }
    };
    private String title;
    private String message;
    private boolean isNoTrackers;
    
    public TrackersMessageModel(String title, String message, boolean isNoTrackers) {
        this.title = title;
        this.message = message;
        this.isNoTrackers = isNoTrackers;
    }
    
    public TrackersMessageModel() {
    
    }
    
    protected TrackersMessageModel(Parcel in) {
        title = in.readString();
        message = in.readString();
        isNoTrackers = in.readByte() != 0;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String data) {
        this.message = data;
    }
    
    public boolean isNoTrackers() {
        return isNoTrackers;
    }
    
    public void setNoTrackers(boolean noTrackers) {
        isNoTrackers = noTrackers;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        
        dest.writeString(title);
        dest.writeString(message);
        dest.writeByte((byte) (isNoTrackers ? 1 : 0));
    }
}
