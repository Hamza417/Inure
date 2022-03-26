package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "batch_state_data")
public class BatchModel implements Parcelable {
    
    @PrimaryKey
    @ColumnInfo (name = "package_name")
    @NonNull
    String packageName;
    
    @ColumnInfo (name = "selected")
    boolean isSelected;
    
    @ColumnInfo (name = "date_selected")
    long dateSelected;
    
    public BatchModel(@NonNull String packageName, boolean isSelected, long dateSelected) {
        this.packageName = packageName;
        this.isSelected = isSelected;
        this.dateSelected = dateSelected;
    }
    
    protected BatchModel(Parcel in) {
        packageName = in.readString();
        isSelected = in.readByte() != 0;
        dateSelected = in.readLong();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeLong(dateSelected);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <BatchModel> CREATOR = new Creator <>() {
        @Override
        public BatchModel createFromParcel(Parcel in) {
            return new BatchModel(in);
        }
        
        @Override
        public BatchModel[] newArray(int size) {
            return new BatchModel[size];
        }
    };
    
    @NonNull
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(@NonNull String packageName) {
        this.packageName = packageName;
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    
    public long getDateSelected() {
        return dateSelected;
    }
    
    public void setDateSelected(long dateSelected) {
        this.dateSelected = dateSelected;
    }
}
