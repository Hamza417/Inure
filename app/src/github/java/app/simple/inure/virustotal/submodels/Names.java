package app.simple.inure.virustotal.submodels;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Names implements Parcelable {
    
    private List <String> names;
    
    protected Names(Parcel in) {
        names = in.createStringArrayList();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(names);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <Names> CREATOR = new Creator <>() {
        @Override
        public Names createFromParcel(Parcel in) {
            return new Names(in);
        }
        
        @Override
        public Names[] newArray(int size) {
            return new Names[size];
        }
    };
    
    public List <String> getNames() {
        return names;
    }
    
    public void setNames(List <String> names) {
        this.names = names;
    }
}
