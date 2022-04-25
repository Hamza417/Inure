package app.simple.inure.models;

import android.content.pm.PackageInfo;
import android.text.Spannable;

public class NotesPackageInfo {
    
    private PackageInfo packageInfo;
    private Spannable note;
    private long dateCreated;
    private long dateUpdated;
    
    public NotesPackageInfo(PackageInfo packageInfo, Spannable note, long dateCreated, long dateUpdated) {
        this.packageInfo = packageInfo;
        this.note = note;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }
    
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
    
    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }
    
    public Spannable getNote() {
        return note;
    }
    
    public void setNote(Spannable note) {
        this.note = note;
    }
    
    public long getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public long getDateUpdated() {
        return dateUpdated;
    }
    
    public void setDateUpdated(long dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
    
}
