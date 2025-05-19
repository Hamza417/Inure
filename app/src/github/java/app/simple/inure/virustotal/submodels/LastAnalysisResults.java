package app.simple.inure.virustotal.submodels;

import java.io.Serializable;
import java.util.Map;

public class LastAnalysisResults implements Serializable {
    
    private Map <String, AnalysisResult> results;
    
    public Map <String, AnalysisResult> getResults() {
        return results;
    }
    
    public void setResults(Map <String, AnalysisResult> results) {
        this.results = results;
    }
}
