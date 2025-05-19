package app.simple.inure.virustotal;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import app.simple.inure.virustotal.submodels.AnalysisResult;
import app.simple.inure.virustotal.submodels.LastAnalysisStats;
import app.simple.inure.virustotal.submodels.TotalVotes;

public class VirusTotalResponse {
    
    @SerializedName ("last_analysis_stats")
    private LastAnalysisStats lastAnalysisStats;
    @SerializedName ("last_analysis_results")
    private Map <String, AnalysisResult> lastAnalysisResults;
    @SerializedName ("total_votes")
    private TotalVotes totalVotes;
    @SerializedName ("names")
    private List <String> names;
    @SerializedName ("first_submission_date")
    private long firstSubmissionDate;
    @SerializedName ("last_submission_date")
    private long lastSubmissionDate;
    @SerializedName ("meaningful_name")
    private String meaningfulName;
    @SerializedName ("times_submitted")
    private int timesSubmitted;
    @SerializedName ("sha256")
    private String sha256;
    @SerializedName ("sha1")
    private String sha1;
    @SerializedName ("md5")
    private String md5;
    
    public LastAnalysisStats getLastAnalysisStats() {
        return lastAnalysisStats;
    }
    
    public void setLastAnalysisStats(LastAnalysisStats lastAnalysisStats) {
        this.lastAnalysisStats = lastAnalysisStats;
    }
    
    public Map <String, AnalysisResult> getLastAnalysisResults() {
        return lastAnalysisResults;
    }
    
    public void setLastAnalysisResults(Map <String, AnalysisResult> lastAnalysisResults) {
        this.lastAnalysisResults = lastAnalysisResults;
    }
    
    public TotalVotes getTotalVotes() {
        return totalVotes;
    }
    
    public void setTotalVotes(TotalVotes totalVotes) {
        this.totalVotes = totalVotes;
    }
    
    public List <String> getNames() {
        return names;
    }
    
    public void setNames(List <String> names) {
        this.names = names;
    }
    
    public long getFirstSubmissionDate() {
        return firstSubmissionDate;
    }
    
    public void setFirstSubmissionDate(long firstSubmissionDate) {
        this.firstSubmissionDate = firstSubmissionDate;
    }
    
    public long getLastSubmissionDate() {
        return lastSubmissionDate;
    }
    
    public void setLastSubmissionDate(long lastSubmissionDate) {
        this.lastSubmissionDate = lastSubmissionDate;
    }
    
    public String getMeaningfulName() {
        return meaningfulName;
    }
    
    public void setMeaningfulName(String meaningfulName) {
        this.meaningfulName = meaningfulName;
    }
    
    public int getTimesSubmitted() {
        return timesSubmitted;
    }
    
    public void setTimesSubmitted(int timesSubmitted) {
        this.timesSubmitted = timesSubmitted;
    }
    
    public String getSha256() {
        return sha256;
    }
    
    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }
    
    public String getSha1() {
        return sha1;
    }
    
    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "VirusTotalResponse{" +
                "lastAnalysisStats=" + lastAnalysisStats +
                ", lastAnalysisResults=" + lastAnalysisResults +
                ", totalVotes=" + totalVotes +
                ", names=" + names +
                ", firstSubmissionDate=" + firstSubmissionDate +
                ", lastSubmissionDate=" + lastSubmissionDate +
                ", meaningfulName='" + meaningfulName + '\'' +
                ", timesSubmitted=" + timesSubmitted +
                ", sha256='" + sha256 + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
