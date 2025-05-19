package app.simple.inure.virustotal.submodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class LastAnalysisStats implements Parcelable {
    
    private int malicious;
    private int suspicious;
    private int undetected;
    private int harmless;
    private int timeout;
    private int confirmedTimeout;
    private int failure;
    private int typeUnsupported;
    
    protected LastAnalysisStats(Parcel in) {
        malicious = in.readInt();
        suspicious = in.readInt();
        undetected = in.readInt();
        harmless = in.readInt();
        timeout = in.readInt();
        confirmedTimeout = in.readInt();
        failure = in.readInt();
        typeUnsupported = in.readInt();
    }
    
    public static final Creator <LastAnalysisStats> CREATOR = new Creator <>() {
        @Override
        public LastAnalysisStats createFromParcel(Parcel in) {
            return new LastAnalysisStats(in);
        }
        
        @Override
        public LastAnalysisStats[] newArray(int size) {
            return new LastAnalysisStats[size];
        }
    };
    
    // Getters
    public int getMalicious() {
        return malicious;
    }
    
    public int getSuspicious() {
        return suspicious;
    }
    
    public int getUndetected() {
        return undetected;
    }
    
    public int getHarmless() {
        return harmless;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public int getConfirmedTimeout() {
        return confirmedTimeout;
    }
    
    public int getFailure() {
        return failure;
    }
    
    public int getTypeUnsupported() {
        return typeUnsupported;
    }
    
    // Setters
    public void setMalicious(int malicious) {
        this.malicious = malicious;
    }
    
    public void setSuspicious(int suspicious) {
        this.suspicious = suspicious;
    }
    
    public void setUndetected(int undetected) {
        this.undetected = undetected;
    }
    
    public void setHarmless(int harmless) {
        this.harmless = harmless;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public void setConfirmedTimeout(int confirmedTimeout) {
        this.confirmedTimeout = confirmedTimeout;
    }
    
    public void setFailure(int failure) {
        this.failure = failure;
    }
    
    public void setTypeUnsupported(int typeUnsupported) {
        this.typeUnsupported = typeUnsupported;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        
        dest.writeInt(malicious);
        dest.writeInt(suspicious);
        dest.writeInt(undetected);
        dest.writeInt(harmless);
        dest.writeInt(timeout);
        dest.writeInt(confirmedTimeout);
        dest.writeInt(failure);
        dest.writeInt(typeUnsupported);
    }
    
    @NonNull
    @Override
    public String toString() {
        return "LastAnalysisStats{" +
                "malicious=" + malicious +
                ", suspicious=" + suspicious +
                ", undetected=" + undetected +
                ", harmless=" + harmless +
                ", timeout=" + timeout +
                ", confirmedTimeout=" + confirmedTimeout +
                ", failure=" + failure +
                ", typeUnsupported=" + typeUnsupported +
                '}';
    }
}
