package app.simple.inure.crash;

import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import androidx.annotation.NonNull;
import app.simple.inure.activities.app.CrashReporterActivity;
import app.simple.inure.preferences.CrashPreferences;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on December 15, 2021
 * Ref: https://stackoverflow.com/questions/601503/how-do-i-obtain-crash-data-from-my-android-application
 */
public class CrashReporterUtils implements Thread.UncaughtExceptionHandler {
    
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;
    
    public CrashReporterUtils(Context context) {
        this.context = context;
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }
    
    public void uncaughtException(@NonNull Thread t, Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        
        Utils.create(stacktrace, new File(context.getExternalFilesDir("logs"), "crashLog_" + Utils.getTimeStamp()));
        CrashPreferences.INSTANCE.saveCrashLog(Utils.getTimeStamp());
        
        Intent intent = new Intent(context, CrashReporterActivity.class);
        intent.putExtra("crashLog", stacktrace);
        context.startActivity(intent);
        
        defaultUncaughtExceptionHandler.uncaughtException(t, e);
    }
    
    public void initialize() {
        String timeStamp = CrashPreferences.INSTANCE.getCrashLog();
        if (timeStamp != null) {
            Intent intent = new Intent(context, CrashReporterActivity.class);
            intent.putExtra("crashLog", Utils.read(new File(context.getExternalFilesDir("logs"), "crashLog_" + timeStamp)));
            context.startActivity(intent);
        }
        Thread.setDefaultUncaughtExceptionHandler(new CrashReporterUtils(context));
    }
}