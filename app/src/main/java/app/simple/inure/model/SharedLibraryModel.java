package app.simple.inure.model;

import android.content.pm.SharedLibraryInfo;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

public class SharedLibraryModel implements Parcelable {
    
    private SharedLibraryInfo sharedLibraryInfo;
    private String status;
    
    public SharedLibraryModel(SharedLibraryInfo sharedLibraryInfo, String status) {
        this.sharedLibraryInfo = sharedLibraryInfo;
        this.status = status;
    }
    
    @RequiresApi (api = Build.VERSION_CODES.O)
    protected SharedLibraryModel(Parcel in) {
        sharedLibraryInfo = in.readParcelable(SharedLibraryInfo.class.getClassLoader());
        status = in.readString();
    }
    
    @RequiresApi (api = Build.VERSION_CODES.O)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(sharedLibraryInfo, flags);
        dest.writeString(status);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <SharedLibraryModel> CREATOR = new Creator <SharedLibraryModel>() {
        @RequiresApi (api = Build.VERSION_CODES.O)
        @Override
        public SharedLibraryModel createFromParcel(Parcel in) {
            return new SharedLibraryModel(in);
        }
        
        @Override
        public SharedLibraryModel[] newArray(int size) {
            return new SharedLibraryModel[size];
        }
    };
    
    public SharedLibraryInfo getSharedLibraryInfo() {
        return sharedLibraryInfo;
    }
    
    public void setSharedLibraryInfo(SharedLibraryInfo sharedLibraryInfo) {
        this.sharedLibraryInfo = sharedLibraryInfo;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
