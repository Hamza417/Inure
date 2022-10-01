package app.simple.inure.crash;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import app.simple.inure.activities.app.CrashReporterActivity;
import app.simple.inure.database.instances.StackTraceDatabase;
import app.simple.inure.models.StackTrace;
import app.simple.inure.preferences.CrashPreferences;

/*
 * Ref: https://stackoverflow.com/questions/601503/how-do-i-obtain-crash-data-from-my-android-application
 */
public class CrashReporterUtils implements Thread.UncaughtExceptionHandler {
    
    private final String TAG = getClass().getSimpleName();
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;
    
    public CrashReporterUtils(Context context) {
        this.context = context;
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }
    
    public void uncaughtException(@NonNull Thread thread, Throwable exception) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        exception.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        
        saveTraceToDataBase(stacktrace);
        Utils.create(stacktrace, new File(context.getExternalFilesDir("logs"), "crashLog_" + Utils.getTimeStamp()));
        CrashPreferences.INSTANCE.saveCrashLog(Utils.getTimeStamp());
        
        Intent intent = new Intent(context, CrashReporterActivity.class);
        intent.putExtra("crashLog", stacktrace);
        context.startActivity(intent);
        
        defaultUncaughtExceptionHandler.uncaughtException(thread, exception);
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
    
    public void saveTraceToDataBase(String trace) {
        Executors.newSingleThreadExecutor().submit(() -> {
            StackTrace stackTrace = new StackTrace(trace, System.currentTimeMillis());
            StackTraceDatabase stackTraceDatabase = StackTraceDatabase.Companion.getInstance(context);
            stackTraceDatabase.stackTraceDao().insertTrace(stackTrace);
            Log.d(TAG, "Trace saved to database");
        });
    }
    
}