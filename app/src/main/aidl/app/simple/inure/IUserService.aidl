package app.simple.inure;

import app.simple.inure.util.ExecuteResult;
import app.simple.inure.IAppOpsActiveCallback;
import android.os.ParcelFileDescriptor;

interface IUserService {

    void destroy() = 16777114; // Destroy method defined by Shizuku server

    void exit() = 1; // Exit method defined by user

    ExecuteResult execute(in List<String> cmdarray, in List<String> envp, String dir) = 2;

    ExecuteResult simpleExecute(in String command) = 3;

    boolean forceStopApp(in String packageName) = 4;

    ExecuteResult executeInputStream(in List<String> cmdarray, in List<String> envp, String dir, in ParcelFileDescriptor inputPipe) = 5;

    boolean install(in List<String> paths, in List<String> opt) = 6;

    /**
     * Start watching active app operations.
     * @param ops Array of operation codes to watch
     * @param callback Callback to receive active state changes
     */
    void startWatchingActive(in int[] ops, IAppOpsActiveCallback callback) = 7;

    /**
     * Stop watching active app operations.
     * @param callback The callback to remove
     */
    void stopWatchingActive(IAppOpsActiveCallback callback) = 8;
}
