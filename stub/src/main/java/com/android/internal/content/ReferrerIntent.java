package com.android.internal.content;

import android.content.Intent;
import android.os.Parcel;

public class ReferrerIntent extends Intent {
    
    public final String mReferrer;
    
    public ReferrerIntent(Intent baseIntent, String referrer) {
        throw new RuntimeException();
    }
    
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        throw new RuntimeException();
    }
    
    ReferrerIntent(Parcel in) {
        throw new RuntimeException();
    }
    
    public static final Creator <ReferrerIntent> CREATOR = new Creator <ReferrerIntent>() {
        public ReferrerIntent createFromParcel(Parcel source) {
            throw new RuntimeException();
        }
        
        public ReferrerIntent[] newArray(int size) {
            throw new RuntimeException();
        }
    };
    
    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException();
    }
    
    @Override
    public int hashCode() {
        throw new RuntimeException();
    }
}
