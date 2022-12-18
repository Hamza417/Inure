package app.simple.inure.models;

import android.util.ArraySet;

import app.simple.inure.interfaces.utils.Copyable;

public class BootManagerModel implements Copyable <BootManagerModel> {
    
    private String packageName;
    private ArraySet <String> disabledComponents = new ArraySet <>();
    private ArraySet <String> enabledComponents = new ArraySet <>();
    private String name;
    private boolean enabled;
    
    public BootManagerModel(String packageName, ArraySet <String> disabledComponents, ArraySet <String> enabledComponents, String name, boolean enabled) {
        this.packageName = packageName;
        this.disabledComponents = disabledComponents;
        this.enabledComponents = enabledComponents;
        this.name = name;
        this.enabled = enabled;
    }
    
    public BootManagerModel() {
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public ArraySet <String> getDisabledComponents() {
        return disabledComponents;
    }
    
    public void setDisabledComponents(ArraySet <String> disabledComponents) {
        this.disabledComponents = disabledComponents;
    }
    
    public void addDisabledComponent(String disabledComponent) {
        try {
            this.disabledComponents.add(disabledComponent);
        } catch (NullPointerException e) {
            this.disabledComponents = new ArraySet <>();
            this.disabledComponents.add(disabledComponent);
        }
    }
    
    public ArraySet <String> getEnabledComponents() {
        return enabledComponents;
    }
    
    public void setEnabledComponents(ArraySet <String> enabledComponents) {
        this.enabledComponents = enabledComponents;
    }
    
    public void addEnabledComponent(String component) {
        try {
            this.enabledComponents.add(component);
        } catch (NullPointerException e) {
            this.enabledComponents = new ArraySet <>();
            this.enabledComponents.add(component);
        }
    }
    
    public String getAllComponentNames() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String component : enabledComponents) {
            stringBuilder.append("• ").append(component).append("\n");
        }
        for (String component : disabledComponents) {
            stringBuilder.append("• ").append(component).append("\n");
        }
        return stringBuilder.toString().trim();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public BootManagerModel copy() {
        return new BootManagerModel(packageName, disabledComponents, enabledComponents, name, enabled);
    }
    
    @Override
    public BootManagerModel createForCopy() {
        return new BootManagerModel();
    }
    
    @Override
    public void copyTo(BootManagerModel dest) {
        dest.setPackageName(packageName);
        dest.setDisabledComponents(disabledComponents);
        dest.setEnabledComponents(enabledComponents);
        dest.setName(name);
        dest.setEnabled(enabled);
    }
}
