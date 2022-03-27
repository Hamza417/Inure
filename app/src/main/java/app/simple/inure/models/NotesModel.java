package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "app_notes_data")
public class NotesModel implements Parcelable {
    
    @NonNull
    @PrimaryKey
    @ColumnInfo (name = "package_id")
    private String packageName;
    
    @ColumnInfo (name = "note")
    private String note;
    
    @ColumnInfo (name = "date_created")
    private long dateCreated;
    
    @ColumnInfo (name = "date_changed")
    private long dateChanged;
    
    public NotesModel(String note, @NonNull String packageName, long dateCreated, long dateChanged) {
        this.note = note;
        this.packageName = packageName;
        this.dateCreated = dateCreated;
        this.dateChanged = dateChanged;
    }
    
    protected NotesModel(Parcel in) {
        note = in.readString();
        packageName = in.readString();
        dateCreated = in.readLong();
        dateChanged = in.readLong();
    }
    
    public static final Creator <NotesModel> CREATOR = new Creator <NotesModel>() {
        @Override
        public NotesModel createFromParcel(Parcel in) {
            return new NotesModel(in);
        }
        
        @Override
        public NotesModel[] newArray(int size) {
            return new NotesModel[size];
        }
    };
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    @NonNull
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(@NonNull String packageName) {
        this.packageName = packageName;
    }
    
    public long getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public long getDateChanged() {
        return dateChanged;
    }
    
    public void setDateChanged(long dateChanged) {
        this.dateChanged = dateChanged;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(note);
        dest.writeString(packageName);
        dest.writeLong(dateCreated);
        dest.writeLong(dateChanged);
    }
}