package app.simple.inure.trackers.reflector;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedList;
import java.util.List;

public class ClassesNamesList implements Parcelable {
    private final List <String> list;
    
    public ClassesNamesList() {
        list = new LinkedList <>();
    }
    
    protected ClassesNamesList(Parcel in) {
        list = in.createStringArrayList();
    }
    
    public static final Creator <ClassesNamesList> CREATOR = new Creator <>() {
        @Override
        public ClassesNamesList createFromParcel(Parcel in) {
            return new ClassesNamesList(in);
        }
        
        @Override
        public ClassesNamesList[] newArray(int size) {
            return new ClassesNamesList[size];
        }
    };
    
    public void add(String className) {
        list.add(className);
    }
    
    public int size() {
        return this.list.size();
    }
    
    public List <String> getClassNames() {
        return this.list;
    }
    
    public String getClassName(int position) {
        return this.list.get(position);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(list);
    }
}