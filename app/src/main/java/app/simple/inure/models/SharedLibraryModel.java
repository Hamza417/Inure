package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SharedLibraryModel implements Parcelable {
    
    private String name;
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
    private long size;
    
    public SharedLibraryModel(String name, long size) {
        this.name = name;
        this.size = size;
    }
    
    public SharedLibraryModel() {
    }
    
    protected SharedLibraryModel(Parcel in) {
        name = in.readString();
        size = in.readInt();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        
        parcel.writeString(name);
        parcel.writeLong(size);
    }
}