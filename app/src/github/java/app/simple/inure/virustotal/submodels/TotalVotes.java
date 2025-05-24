package app.simple.inure.virustotal.submodels;

import androidx.annotation.NonNull;

public class TotalVotes {
    
    private int harmless;
    private int malicious;
    
    // Getters
    public int getHarmless() {
        return harmless;
    }
    
    public int getMalicious() {
        return malicious;
    }
    
    // Setters
    public void setHarmless(int harmless) {
        this.harmless = harmless;
    }
    
    public void setMalicious(int malicious) {
        this.malicious = malicious;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "TotalVotes{" +
                "harmless=" + harmless +
                ", malicious=" + malicious +
                '}';
    }
}
