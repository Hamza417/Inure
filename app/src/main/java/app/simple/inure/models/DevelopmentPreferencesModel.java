package app.simple.inure.models;

public class DevelopmentPreferencesModel {
    
    public static final int TYPE_BOOLEAN = 0;
    public static final int TYPE_INTEGER = 1;
    public static final int TYPE_STRING = 2;
    public static final int TYPE_FLOAT = 3;
    public static final int TYPE_LONG = 4;
    public static final int TYPE_FRAGMENT = 5;
    
    private String label;
    private String description;
    private String key;
    private int type;
    
    public DevelopmentPreferencesModel(String label, String description, String key, int type) {
        this.label = label;
        this.description = description;
        this.key = key;
        this.type = type;
    }
    
    public String getTitle() {
        return label;
    }
    
    public void setTitle(String label) {
        this.label = label;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
