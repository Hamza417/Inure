package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Analytics implements Parcelable {
    public static final Creator <Analytics> CREATOR = new Creator <Analytics>() {
        @Override
        public Analytics createFromParcel(Parcel in) {
            return new Analytics(in);
        }
        
        @Override
        public Analytics[] newArray(int size) {
            return new Analytics[size];
        }
    };
    private String label = "";
    private int code = 0;
    private int count = 0;
    
    public Analytics(String label, int code, int count) {
        this.label = label;
        this.code = code;
        this.count = count;
    }
    
    protected Analytics(Parcel in) {
        label = in.readString();
        code = in.readInt();
        count = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(label);
        dest.writeInt(code);
        dest.writeInt(count);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
}
