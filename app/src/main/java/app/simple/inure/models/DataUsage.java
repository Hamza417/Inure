package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import app.simple.inure.util.FileSizeHelper;

public class DataUsage extends Pair <Long, Long> implements Parcelable, Comparable <DataUsage> {
    
    public static final DataUsage EMPTY = new DataUsage(0, 0);
    private final long mTotal;
    
    public DataUsage(long tx, long rx) {
        super(tx, rx);
        mTotal = tx + rx;
    }
    
    private DataUsage(@NonNull Parcel in) {
        super(in.readLong(), in.readLong());
        mTotal = first + second;
    }
    
    public static final Creator <DataUsage> CREATOR = new Creator <>() {
        @NonNull
        @Override
        public DataUsage createFromParcel(Parcel in) {
            return new DataUsage(in);
        }
        
        @NonNull
        @Override
        public DataUsage[] newArray(int size) {
            return new DataUsage[size];
        }
    };
    
    public long getTx() {
        return first;
    }
    
    /**
     * Data received
     *
     * @return
     */
    public long getRx() {
        return second;
    }
    
    public long getTotal() {
        return mTotal;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(first);
        dest.writeLong(second);
    }
    
    @Override
    public int compareTo(@Nullable DataUsage o) {
        if (o == null) {
            return 1;
        }
        return Long.compare(mTotal, o.mTotal);
    }
    
    @NonNull
    @Override
    public String toString() {
        return "↓ " + FileSizeHelper.INSTANCE.toSize(first)
                + " : ↑ " + FileSizeHelper.INSTANCE.toSize(second);
    }
}