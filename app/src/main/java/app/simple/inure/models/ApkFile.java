package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

import androidx.annotation.NonNull;

public class ApkFile implements Parcelable {
    
    public static final Creator <ApkFile> CREATOR = new Creator <>() {
        @Override
        public ApkFile createFromParcel(Parcel in) {
            return new ApkFile(in);
        }
        
        @Override
        public ApkFile[] newArray(int size) {
            return new ApkFile[size];
        }
    };
    private File file;
    private boolean isSelected = false;
    
    public ApkFile(File file) {
        this.file = file;
    }
    
    protected ApkFile(Parcel in) {
        isSelected = in.readByte() != 0;
    }
    
    public File getFile() {
        return file;
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeByte((byte) (isSelected ? 1 : 0));
        parcel.writeSerializable(file);
    }
}
