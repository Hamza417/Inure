package app.simple.inure.virustotal.submodels;

import androidx.annotation.NonNull;

/**
 * Represents the result of an antivirus engine analysis.
 *
 * <p>
 * The result contains the following fields:
 * </p>
 * <ul>
 *   <li><b>category</b>: <code>String</code> - Normalized result. Possible values:
 *     <ul>
 *       <li><code>confirmed-timeout</code>: AV reached a timeout when analyzing the file (file analyses only).</li>
 *       <li><code>timeout</code>: AV reached a timeout when analyzing the file.</li>
 *       <li><code>failure</code>: AV failed when analyzing the file (file analyses only).</li>
 *       <li><code>harmless</code>: AV thinks the file is not malicious.</li>
 *       <li><code>undetected</code>: AV has no opinion about this file.</li>
 *       <li><code>suspicious</code>: AV thinks the file is suspicious.</li>
 *       <li><code>malicious</code>: AV thinks the file is malicious.</li>
 *       <li><code>type-unsupported</code>: AV can't analyze the file (file analyses only).</li>
 *     </ul>
 *   </li>
 *   <li><b>engine_name</b>: <code>String</code> - The engine's name.</li>
 *   <li><b>engine_update</b>: <code>String</code> - The engine's update date in <code>%Y%M%D</code> format (file analyses only).</li>
 *   <li><b>engine_version</b>: <code>String</code> - The engine's version (file analyses only).</li>
 *   <li><b>method</b>: <code>String</code> - Detection method.</li>
 *   <li><b>result</b>: <code>String</code> - Engine result. Can be <code>null</code> if no verdict is available.</li>
 * </ul>
 */
public final class AnalysisResult {
    
    private String method;
    private String engineName;
    private String engineVersion;
    private String engineUpdate;
    private String category;
    private String result;
    
    /**
     * The category of the analysis result.
     */
    public static final String CATEGORY_CONFIRMED_TIMEOUT = "confirmed-timeout";
    public static final String CATEGORY_TIMEOUT = "timeout";
    public static final String CATEGORY_FAILURE = "failure";
    public static final String CATEGORY_HARMLESS = "harmless";
    public static final String CATEGORY_UNDETECTED = "undetected";
    public static final String CATEGORY_SUSPICIOUS = "suspicious";
    public static final String CATEGORY_MALICIOUS = "malicious";
    public static final String CATEGORY_TYPE_UNSUPPORTED = "type-unsupported";
    
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
    
    @NonNull
    @Override
    public String toString() {
        return "AnalysisResult{" +
                "method='" + method + '\'' +
                ", engineName='" + engineName + '\'' +
                ", engineVersion='" + engineVersion + '\'' +
                ", engineUpdate='" + engineUpdate + '\'' +
                ", category='" + category + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
