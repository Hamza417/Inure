package app.simple.inure.models;

import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

import androidx.annotation.NonNull;

public class Tracker implements Parcelable {
    
    /**
     * Basic tracker info
     */
    private String name;
    private String codeSignature;
    private String networkSignature;
    private String creationDate;
    private String description;
    private String website;
    private String[] categories;
    private String[] documentation;
    
    /**
     * Component info
     */
    private boolean isActivity = false;
    private boolean isService = false;
    private boolean isReceiver = false;
    private boolean isBlocked = false;
    private boolean isEnabled = true;
    private ActivityInfo activityInfo = null;
    private ActivityInfo receiverInfo = null;
    private ServiceInfo serviceInfo = null;
    private String componentName;
    public boolean isLogged = false; // I don't know why I added this
    
    public Tracker() {
    }
    
    protected Tracker(Parcel in) {
        name = in.readString();
        codeSignature = in.readString();
        networkSignature = in.readString();
        creationDate = in.readString();
        description = in.readString();
        website = in.readString();
        categories = in.createStringArray();
        documentation = in.createStringArray();
        componentName = in.readString();
        isActivity = in.readByte() != 0;
        isService = in.readByte() != 0;
        isReceiver = in.readByte() != 0;
        isBlocked = in.readByte() != 0;
        isEnabled = in.readByte() != 0;
        activityInfo = in.readParcelable(ActivityInfo.class.getClassLoader());
        receiverInfo = in.readParcelable(ActivityInfo.class.getClassLoader());
        serviceInfo = in.readParcelable(ServiceInfo.class.getClassLoader());
        isLogged = in.readByte() != 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(codeSignature);
        dest.writeString(networkSignature);
        dest.writeString(creationDate);
        dest.writeString(description);
        dest.writeString(website);
        dest.writeStringArray(categories);
        dest.writeStringArray(documentation);
        dest.writeString(componentName);
        dest.writeByte((byte) (isActivity ? 1 : 0));
        dest.writeByte((byte) (isService ? 1 : 0));
        dest.writeByte((byte) (isReceiver ? 1 : 0));
        dest.writeByte((byte) (isBlocked ? 1 : 0));
        dest.writeByte((byte) (isEnabled ? 1 : 0));
        dest.writeParcelable(activityInfo, flags);
        dest.writeParcelable(receiverInfo, flags);
        dest.writeParcelable(serviceInfo, flags);
        dest.writeByte((byte) (isLogged ? 1 : 0));
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <Tracker> CREATOR = new Creator <>() {
        @Override
        public Tracker createFromParcel(Parcel in) {
            return new Tracker(in);
        }
        
        @Override
        public Tracker[] newArray(int size) {
            return new Tracker[size];
        }
    };
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCodeSignature() {
        return codeSignature;
    }
    
    public void setCodeSignature(String codeSignature) {
        this.codeSignature = codeSignature;
    }
    
    public String getNetworkSignature() {
        return networkSignature;
    }
    
    public void setNetworkSignature(String networkSignature) {
        this.networkSignature = networkSignature;
    }
    
    public String getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String[] getCategories() {
        return categories;
    }
    
    public void setCategories(String[] categories) {
        this.categories = categories;
    }
    
    public boolean isActivity() {
        return isActivity;
    }
    
    public void setActivity(boolean activity) {
        isActivity = activity;
    }
    
    public boolean isService() {
        return isService;
    }
    
    public void setService(boolean service) {
        isService = service;
    }
    
    public boolean isReceiver() {
        return isReceiver;
    }
    
    public void setReceiver(boolean receiver) {
        isReceiver = receiver;
    }
    
    public boolean isBlocked() {
        return isBlocked;
    }
    
    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
    
    public ActivityInfo getActivityInfo() {
        return activityInfo;
    }
    
    public void setActivityInfo(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
    }
    
    public ActivityInfo getReceiverInfo() {
        return receiverInfo;
    }
    
    public void setReceiverInfo(ActivityInfo receiverInfo) {
        this.receiverInfo = receiverInfo;
    }
    
    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }
    
    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
    
    public boolean isLogged() {
        return isLogged;
    }
    
    public void setLogged(boolean logged) {
        isLogged = logged;
    }
    
    public String[] getDocumentation() {
        return documentation;
    }
    
    public void setDocumentation(String[] documentation) {
        this.documentation = documentation;
    }
    
    public String getComponentName() {
        return componentName;
    }
    
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "Tracker{" +
                "name='" + name + '\'' +
                ", codeSignature='" + codeSignature + '\'' +
                ", networkSignature='" + networkSignature + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", description='" + description + '\'' +
                ", website='" + website + '\'' +
                ", categories=" + Arrays.toString(categories) +
                ", documentation=" + Arrays.toString(documentation) +
                ", componentName='" + componentName + '\'' +
                ", isActivity=" + isActivity +
                ", isService=" + isService +
                ", isReceiver=" + isReceiver +
                ", isBlocked=" + isBlocked +
                ", isEnabled=" + isEnabled +
                ", activityInfo=" + activityInfo +
                ", receiverInfo=" + receiverInfo +
                ", serviceInfo=" + serviceInfo +
                ", isLogged=" + isLogged +
                '}';
    }
    
    // Copy all the info into tracker
    public void copy(Tracker tracker) {
        tracker.setName(getName());
        tracker.setCodeSignature(getCodeSignature());
        tracker.setNetworkSignature(getNetworkSignature());
        tracker.setCreationDate(getCreationDate());
        tracker.setDescription(getDescription());
        tracker.setWebsite(getWebsite());
        tracker.setCategories(getCategories());
        tracker.setDocumentation(getDocumentation());
        tracker.setComponentName(getComponentName());
        tracker.setActivity(isActivity());
        tracker.setService(isService());
        tracker.setReceiver(isReceiver());
        tracker.setBlocked(isBlocked());
        tracker.setEnabled(isEnabled());
        tracker.setActivityInfo(getActivityInfo());
        tracker.setReceiverInfo(getReceiverInfo());
        tracker.setServiceInfo(getServiceInfo());
        tracker.setLogged(isLogged());
    }
    
    // Copy all the basic info into tracker
    public void copyBasicTrackerInfo(Tracker tracker) {
        tracker.setName(getName());
        tracker.setCodeSignature(getCodeSignature());
        tracker.setNetworkSignature(getNetworkSignature());
        tracker.setCreationDate(getCreationDate());
        tracker.setDescription(getDescription());
        tracker.setWebsite(getWebsite());
        tracker.setCategories(getCategories());
        tracker.setDocumentation(getDocumentation());
    }
    
    // Copy all the component info into tracker
    public void copyComponentInfo(Tracker tracker) {
        tracker.setComponentName(getComponentName());
        tracker.setActivity(isActivity());
        tracker.setService(isService());
        tracker.setReceiver(isReceiver());
        tracker.setBlocked(isBlocked());
        tracker.setEnabled(isEnabled());
        tracker.setActivityInfo(getActivityInfo());
        tracker.setReceiverInfo(getReceiverInfo());
        tracker.setServiceInfo(getServiceInfo());
        tracker.setLogged(isLogged());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        Tracker tracker = (Tracker) o;
        
        if (isActivity() != tracker.isActivity()) {
            return false;
        }
        if (isService() != tracker.isService()) {
            return false;
        }
        if (isReceiver() != tracker.isReceiver()) {
            return false;
        }
        if (isBlocked() != tracker.isBlocked()) {
            return false;
        }
        if (isEnabled() != tracker.isEnabled()) {
            return false;
        }
        if (isLogged() != tracker.isLogged()) {
            return false;
        }
        if (!getName().equals(tracker.getName())) {
            return false;
        }
        if (!getCodeSignature().equals(tracker.getCodeSignature())) {
            return false;
        }
        if (!getNetworkSignature().equals(tracker.getNetworkSignature())) {
            return false;
        }
        if (!getCreationDate().equals(tracker.getCreationDate())) {
            return false;
        }
        if (!getDescription().equals(tracker.getDescription())) {
            return false;
        }
        if (!getWebsite().equals(tracker.getWebsite())) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getCategories(), tracker.getCategories())) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getDocumentation(), tracker.getDocumentation())) {
            return false;
        }
        if (getActivityInfo() != null) {
            if (!getActivityInfo().equals(tracker.getActivityInfo())) {
                return false;
            }
        }
        if (getReceiverInfo() != null) {
            if (!getReceiverInfo().equals(tracker.getReceiverInfo())) {
                return false;
            }
        }
        if (getServiceInfo() != null) {
            if (!getServiceInfo().equals(tracker.getServiceInfo())) {
                return false;
            }
        }
        return getComponentName().equals(tracker.getComponentName());
    }
    
    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getCodeSignature().hashCode();
        result = 31 * result + getNetworkSignature().hashCode();
        result = 31 * result + getCreationDate().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + getWebsite().hashCode();
        result = 31 * result + Arrays.hashCode(getCategories());
        result = 31 * result + Arrays.hashCode(getDocumentation());
        result = 31 * result + (isActivity() ? 1 : 0);
        result = 31 * result + (isService() ? 1 : 0);
        result = 31 * result + (isReceiver() ? 1 : 0);
        result = 31 * result + (isBlocked() ? 1 : 0);
        result = 31 * result + (isEnabled() ? 1 : 0);
        result = 31 * result + getActivityInfo().hashCode();
        result = 31 * result + getReceiverInfo().hashCode();
        result = 31 * result + getServiceInfo().hashCode();
        result = 31 * result + getComponentName().hashCode();
        result = 31 * result + (isLogged() ? 1 : 0);
        return result;
    }
    
    public boolean isSameTracker(Tracker tracker) {
        return getCodeSignature().equals(tracker.getCodeSignature());
    }
    
    public boolean isSameComponent(Tracker tracker) {
        return getComponentName().equals(tracker.getComponentName());
    }
}
