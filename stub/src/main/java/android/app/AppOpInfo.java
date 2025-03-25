package android.app;

import androidx.annotation.RequiresApi;

@RequiresApi (api = 34)
class AppOpInfo {
    
    public AppOpsManagerHidden.RestrictionBypass allowSystemRestrictionBypass;
    public int code;
    public int defaultMode;
    public boolean disableReset;
    public boolean forceCollectNotes;
    public String name;
    public String permission;
    public boolean restrictRead;
    public String restriction;
    public String simpleName;
    public int switchCode;
}
