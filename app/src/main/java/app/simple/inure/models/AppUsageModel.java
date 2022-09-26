package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

public class AppUsageModel implements Parcelable {
    private long date;
    private long startTime;
    private long endTime;
    
    public AppUsageModel(long date, long startTime, long endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public AppUsageModel() {
    }
    
    protected AppUsageModel(Parcel in) {
        date = in.readLong();
        startTime = in.readLong();
        endTime = in.readLong();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(date);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <AppUsageModel> CREATOR = new Creator <AppUsageModel>() {
        @Override
        public AppUsageModel createFromParcel(Parcel in) {
            return new AppUsageModel(in);
        }
        
        @Override
        public AppUsageModel[] newArray(int size) {
            return new AppUsageModel[size];
        }
    };
    
    public long getDate() {
        return date;
    }
    
    public void setDate(long date) {
        this.date = date;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
}
