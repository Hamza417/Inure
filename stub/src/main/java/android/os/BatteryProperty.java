package android.os;

public class BatteryProperty implements Parcelable {
    
    public long getLong() {
        throw new RuntimeException("STUB");
    }
    
    public void setLong(long val) {
        throw new RuntimeException("STUB");
    }
    
    public void readFromParcel(Parcel p) {
        throw new RuntimeException("STUB");
    }
    
    public void writeToParcel(Parcel p, int flags) {
        throw new RuntimeException("STUB");
    }
    
    public static final Parcelable.Creator <BatteryProperty> CREATOR
            = new Parcelable.Creator <BatteryProperty>() {
        public BatteryProperty createFromParcel(Parcel p) {
            throw new RuntimeException("STUB");
        }
        
        public BatteryProperty[] newArray(int size) {
            throw new RuntimeException("STUB");
        }
    };
    
    public int describeContents() {
        throw new RuntimeException("STUB");
    }
}
