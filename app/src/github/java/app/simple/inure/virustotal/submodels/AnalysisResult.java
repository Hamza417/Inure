package app.simple.inure.virustotal.submodels;

public final class AnalysisResult {
    
    private String method;
    private String engineName;
    private String engineVersion;
    private String engineUpdate;
    private String category;
    private String result; // or Object if it can be null/other types
    
    // Getters
    public String getMethod() {
        return method;
    }
    
    public String getEngineName() {
        return engineName;
    }
    
    public String getEngineVersion() {
        return engineVersion;
    }
    
    public String getEngineUpdate() {
        return engineUpdate;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getResult() {
        return result;
    }
    
    // Setters
    public void setMethod(String method) {
        this.method = method;
    }
    
    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }
    
    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }
    
    public void setEngineUpdate(String engineUpdate) {
        this.engineUpdate = engineUpdate;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
}
