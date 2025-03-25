package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;

import androidx.annotation.RequiresApi;

public class UserInfo {
    
    public static final int FLAG_MANAGED_PROFILE = 0x00000020;
    
    public int id;
    public String name;
    public int flags;
    public int serialNumber;
    
    @RequiresApi (30)
    public String userType;
    
    public boolean isPrimary() {
        throw new RuntimeException("STUB");
    }
    
    public boolean isAdmin() {
        throw new RuntimeException("STUB");
    }
    
    public boolean isGuest() {
        throw new RuntimeException("STUB");
    }
    
    public boolean isRestricted() {
        throw new RuntimeException("STUB");
    }
    
    public boolean isManagedProfile() {
        throw new RuntimeException("STUB");
    }
    
    public boolean isEnabled() {
        throw new RuntimeException("STUB");
    }
    
    public UserHandle getUserHandle() {
        throw new RuntimeException("STUB");
    }
    
    public static final Parcelable.Creator <UserInfo> CREATOR = new Parcelable.Creator <UserInfo>() {
        
        public UserInfo createFromParcel(Parcel in) {
            throw new UnsupportedOperationException();
        }
        
        public UserInfo[] newArray(int size) {
            throw new UnsupportedOperationException();
        }
    };
}
