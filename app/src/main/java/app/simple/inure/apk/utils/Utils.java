package app.simple.inure.apk.utils;

import android.util.Log;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.preferences.ConfigurationPreferences;

public class Utils {
    
    public static void runCommand(String command) {
        if (ConfigurationPreferences.INSTANCE.isUsingRoot() && Shell.rootAccess()) {
            Shell.cmd(command).exec();
        } else {
            try {
                Runtime.getRuntime().exec(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @NonNull
    public static String runAndGetOutput(String command) {
        StringBuilder sb = new StringBuilder();
        try {
            List <String> outputs = Shell.cmd(command).exec().getOut();
            if (ShellUtils.isValidOutput(outputs)) {
                for (String output : outputs) {
                    Log.d("AppOp -> ", output);
                    sb.append(output).append("\n");
                }
            }
            return removeSuffix(sb.toString()).trim();
        } catch (Exception e) {
            return "";
        }
    }
    
    private static String removeSuffix(@Nullable String s) {
        if (s != null && s.endsWith("\n")) {
            return s.substring(0, s.length() - "\n".length());
        }
        return s;
    }
}
