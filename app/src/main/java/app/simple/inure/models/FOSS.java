package app.simple.inure.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "foss")
public class FOSS {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo (name = "package_name")
    private String packageName;
    
    @ColumnInfo (name = "version_code")
    private String versionCode;
    
    @ColumnInfo (name = "is_foss")
    private boolean isFOSS;
    
    public FOSS(@NonNull String packageName, String versionCode, boolean isFOSS) {
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.isFOSS = isFOSS;
    }
    
    @NonNull
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(@NonNull String packageName) {
        this.packageName = packageName;
    }
    
    public String getVersionCode() {
        return versionCode;
    }
    
    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }
    
    public boolean isFOSS() {
        return isFOSS;
    }
    
    public void setFOSS(boolean FOSS) {
        isFOSS = FOSS;
    }
}
