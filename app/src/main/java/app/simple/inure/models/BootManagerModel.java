package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class BootManagerModel implements Parcelable {
    
    private String packageName;
    private ArrayList <String> disabledComponents = new ArrayList <>();
    private ArrayList <String> enabledComponents = new ArrayList <>();
    private String name;
    private boolean enabled;
    
    public BootManagerModel(String packageName, ArrayList <String> disabledComponents, ArrayList <String> enabledComponents, String name, boolean enabled) {
        this.packageName = packageName;
        this.disabledComponents = disabledComponents;
        this.enabledComponents = enabledComponents;
        this.name = name;
        this.enabled = enabled;
    }
    
    public BootManagerModel() {
    }
    
    protected BootManagerModel(Parcel in) {
        packageName = in.readString();
        disabledComponents = in.createStringArrayList();
        enabledComponents = in.createStringArrayList();
        name = in.readString();
        enabled = in.readByte() != 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeStringList(disabledComponents);
        dest.writeStringList(enabledComponents);
        dest.writeString(name);
        dest.writeByte((byte) (enabled ? 1 : 0));
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <BootManagerModel> CREATOR = new Creator <>() {
        @Override
        public BootManagerModel createFromParcel(Parcel in) {
            return new BootManagerModel(in);
        }
        
        @Override
        public BootManagerModel[] newArray(int size) {
            return new BootManagerModel[size];
        }
    };
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public ArrayList <String> getDisabledComponents() {
        return disabledComponents;
    }
    
    public void setDisabledComponents(ArrayList <String> disabledComponents) {
        this.disabledComponents = disabledComponents;
    }
    
    public void addDisabledComponent(String disabledComponent) {
        try {
            this.disabledComponents.add(disabledComponent);
        } catch (NullPointerException e) {
            this.disabledComponents = new ArrayList <>();
            this.disabledComponents.add(disabledComponent);
        }
    }
    
    public ArrayList <String> getEnabledComponents() {
        return enabledComponents;
    }
    
    public void setEnabledComponents(ArrayList <String> enabledComponents) {
        this.enabledComponents = enabledComponents;
    }
    
    public void addEnabledComponent(String component) {
        try {
            this.enabledComponents.add(component);
        } catch (NullPointerException e) {
            this.enabledComponents = new ArrayList <>();
            this.enabledComponents.add(component);
        }
    }
    
    public String getAllComponentNames() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String component : enabledComponents) {
            stringBuilder.append("• ").append(component).append("\n");
        }
        for (String component : disabledComponents) {
            stringBuilder.append("• ").append(component).append("\n");
        }
        return stringBuilder.toString().trim();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
