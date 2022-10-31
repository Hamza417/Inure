package app.simple.inure.apk.ops;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;

import app.simple.inure.apk.utils.PermissionUtils;
import app.simple.inure.apk.utils.Utils;
import app.simple.inure.models.AppOpsModel;

public class AppOps {
    public static ArrayList <AppOpsModel> getOps(Context context, String packageName) {
        ArrayList <AppOpsModel> mData = new ArrayList <>();
        for (String line : Utils.runAndGetOutput("appops get " + packageName).split("\\r?\\n")) {
            String[] splitOp = line.split(":");
            String name = splitOp[0];
            /*
             * We don't need a single "No operations." item if operations are empty.
             * Also, "Uid mode" needs more work (and likely never work)
             */
            if (!line.equals("No operations.") && !name.equals("Uid mode")) {
                mData.add(new AppOpsModel(name, PermissionUtils.INSTANCE.getPermissionDescription(context, name), (line.contains("allow") || line.contains("ignore"))));
            }
        }
        return mData;
    }
    
    public static String getCommandPrefix() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return "cmd";
        } else {
            return "";
        }
    }
}