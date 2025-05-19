package app.simple.inure.virustotal;

import app.simple.inure.virustotal.submodels.LastAnalysisResults;
import app.simple.inure.virustotal.submodels.LastAnalysisStats;
import app.simple.inure.virustotal.submodels.Names;
import app.simple.inure.virustotal.submodels.TotalVotes;

public class VirusTotalResponse {
    
    private LastAnalysisStats lastAnalysisStats;
    private LastAnalysisResults lastAnalysisResults;
    private TotalVotes totalVotes;
    private Names names;
    
    private long firstSubmissionDate;
    private long lastSubmissionDate;
    private String meaningfulName;
    private int timesSubmitted;
    private String sha256;
    private String sha1;
    private String md5;
    
    public LastAnalysisStats getLastAnalysisStats() {
        return lastAnalysisStats;
    }
    
    public void setLastAnalysisStats(LastAnalysisStats lastAnalysisStats) {
        this.lastAnalysisStats = lastAnalysisStats;
    }
    
    public LastAnalysisResults getLastAnalysisResults() {
        return lastAnalysisResults;
    }
    
    public void setLastAnalysisResults(LastAnalysisResults lastAnalysisResults) {
        this.lastAnalysisResults = lastAnalysisResults;
    }
    
    public TotalVotes getTotalVotes() {
        return totalVotes;
    }
    
    public void setTotalVotes(TotalVotes totalVotes) {
        this.totalVotes = totalVotes;
    }
    
    public Names getNames() {
        return names;
    }
    
    public void setNames(Names names) {
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
}
