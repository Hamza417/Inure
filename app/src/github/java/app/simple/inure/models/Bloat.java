package app.simple.inure.models;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.enums.Removal;

public class Bloat implements Parcelable {
    
    private String id;
    private String list;
    private String description;
    private boolean isSelected;
    private ArrayList <String> dependencies;
    private ArrayList <String> neededBy;
    private ArrayList <String> labels;
    private Removal removal;
    private PackageInfo packageInfo;
    
    public Bloat() {
    }
    
    /**
     * @noinspection unused
     */
    public Bloat(String id,
            String list,
            String description,
            boolean isSelected,
            ArrayList <String> dependencies,
            ArrayList <String> neededBy,
            ArrayList <String> labels,
            Removal removal,
            PackageInfo packageInfo) {
        this.id = id;
        this.list = list;
        this.description = description;
        this.isSelected = isSelected;
        this.dependencies = dependencies;
        this.neededBy = neededBy;
        this.labels = labels;
        this.removal = removal;
        this.packageInfo = packageInfo;
    }
    
    protected Bloat(Parcel in) {
        id = in.readString();
        list = in.readString();
        description = in.readString();
        isSelected = in.readByte() != 0;
        dependencies = in.createStringArrayList();
        neededBy = in.createStringArrayList();
        labels = in.createStringArrayList();
        packageInfo = in.readParcelable(PackageInfo.class.getClassLoader());
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(list);
        dest.writeString(description);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeStringList(dependencies);
        dest.writeStringList(neededBy);
        dest.writeStringList(labels);
        dest.writeParcelable(packageInfo, flags);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <Bloat> CREATOR = new Creator <>() {
        @Override
        public Bloat createFromParcel(Parcel in) {
            return new Bloat(in);
        }
        
        @Override
        public Bloat[] newArray(int size) {
            return new Bloat[size];
        }
    };
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getList() {
        return list;
    }
    
    public void setList(String list) {
        this.list = list;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    
    public ArrayList <String> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(ArrayList <String> dependencies) {
        this.dependencies = dependencies;
    }
    
    public ArrayList <String> getNeededBy() {
        return neededBy;
    }
    
    public void setNeededBy(ArrayList <String> neededBy) {
        this.neededBy = neededBy;
    }
    
    public ArrayList <String> getLabels() {
        return labels;
    }
    
    public void setLabels(ArrayList <String> labels) {
        this.labels = labels;
    }
    
    public Removal getRemoval() {
        return removal;
    }
    
    public void setRemoval(Removal removal) {
        this.removal = removal;
    }
    
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
    
    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "Bloat{" +
                "id='" + id + '\'' +
                ", list='" + list + '\'' +
                ", description='" + description + '\'' +
                ", isSelected=" + isSelected +
                ", dependencies=" + dependencies +
                ", neededBy=" + neededBy +
                ", labels=" + labels +
                ", removal=" + removal +
                ", packageInfo=" + packageInfo.toString() +
                '}';
    }
    
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Bloat bloat) {
            return bloat.getId().equals(this.getId());
        }
        
        return false;
    }
}
