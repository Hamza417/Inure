package app.simple.inure.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SharedLibraryModel implements Parcelable {
    
    private String name;
    
    public SharedLibraryModel() {
    
    }
    
    public SharedLibraryModel(String name) {
        this.name = name;
    }
    
    protected SharedLibraryModel(Parcel in) {
        name = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <SharedLibraryModel> CREATOR = new Creator <SharedLibraryModel>() {
        @Override
        public SharedLibraryModel createFromParcel(Parcel in) {
            return new SharedLibraryModel(in);
        }
    
        @Override
        public SharedLibraryModel[] newArray(int size) {
            return new SharedLibraryModel[size];
        }
    };
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}