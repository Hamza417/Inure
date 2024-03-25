package app.simple.inure.apk.ops;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;

import app.simple.inure.apk.utils.PermissionUtils;
import app.simple.inure.models.AppOp;

public class AppOps {
    public static ArrayList <AppOp> getOps(Context context, String packageName) {
        ArrayList <AppOp> ops = new ArrayList <>();
        HashMap <String, String> permissions = PermissionUtils.INSTANCE.getPermissionMap(context);
        
        for (String line : Utils.runAndGetOutput("appops get " + packageName).split("\\r?\\n")) {
            String[] splitOp = line.split(":");
            String name = splitOp[0].trim();
            
            if (!line.equals("No operations.") && !name.equals("Uid mode")) {
                String mode = splitOp[1].split(";")[0].trim();
                String time = null;
                String duration = null;
                String rejectTime = null;
                String id = permissions.get(name);
                
                if (splitOp[1].contains("time=")) {
                    time = splitOp[1].split("time=")[1].split(";")[0].trim();
                }
                
                if (splitOp[1].contains("duration=")) {
                    duration = splitOp[1].split("duration=")[1].split(";")[0].trim();
                }
                
                if (splitOp[1].contains("rejectTime=")) {
                    rejectTime = splitOp[1].split("rejectTime=")[1].trim();
                }
                
                ops.add(new AppOp(name, id, mode.equals("allow"), time, duration, rejectTime));
            }
        }
        return ops;
    }
    
    public static String getCommandPrefix() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return "cmd";
        } else {
            return "";
        }
    }
}
