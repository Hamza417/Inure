package app.simple.inure.models;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class SearchModel implements Parcelable {
    
    private PackageInfo packageInfo;
    private int permissions = 0;
    private int activities = 0;
    private int services = 0;
    private int resources = 0;
    private int receivers = 0;
    private int providers = 0;
    
    public SearchModel(PackageInfo packageInfo, int permissions, int activities, int services, int resources, int receivers, int providers) {
        this.packageInfo = packageInfo;
        this.permissions = permissions;
        this.activities = activities;
        this.services = services;
        this.resources = resources;
        this.receivers = receivers;
        this.providers = providers;
    }
    
    public SearchModel() {
    }
    
    protected SearchModel(Parcel in) {
        packageInfo = in.readParcelable(PackageInfo.class.getClassLoader());
        permissions = in.readInt();
        activities = in.readInt();
        services = in.readInt();
        resources = in.readInt();
        receivers = in.readInt();
        providers = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(packageInfo, flags);
        dest.writeInt(permissions);
        dest.writeInt(activities);
        dest.writeInt(services);
        dest.writeInt(resources);
        dest.writeInt(receivers);
        dest.writeInt(providers);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <SearchModel> CREATOR = new Creator <SearchModel>() {
        @Override
        public SearchModel createFromParcel(Parcel in) {
            return new SearchModel(in);
        }
        
        @Override
        public SearchModel[] newArray(int size) {
            return new SearchModel[size];
        }
    };
    
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
    
    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }
    
    public int getPermissions() {
        return permissions;
    }
    
    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }
    
    public int getActivities() {
        return activities;
    }
    
    public void setActivities(int activities) {
        this.activities = activities;
    }
    
    public int getServices() {
        return services;
    }
    
    public void setServices(int services) {
        this.services = services;
    }
    
    public int getResources() {
        return resources;
    }
    
    public void setResources(int resources) {
        this.resources = resources;
    }
    
    public int getReceivers() {
        return receivers;
    }
    
    public void setReceivers(int receivers) {
        this.receivers = receivers;
    }
    
    public int getProviders() {
        return providers;
    }
    
    public void setProviders(int providers) {
        this.providers = providers;
    }
    
}
