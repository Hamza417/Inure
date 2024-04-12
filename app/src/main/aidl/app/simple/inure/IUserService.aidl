package app.simple.inure;

import app.simple.inure.util.ExecuteResult;
import android.os.ParcelFileDescriptor;

interface IUserService {

    void destroy() = 16777114; // Destroy method defined by Shizuku server

    void exit() = 1; // Exit method defined by user

    ExecuteResult execute(in List<String> cmdarray, in List<String> envp, String dir) = 2;

    ExecuteResult simpleExecute(in String command) = 3;

    boolean forceStopApp(in String packageName) = 4;

    ExecuteResult executeInputStream(in List<String> cmdarray, in List<String> envp, String dir, in ParcelFileDescriptor inputPipe) = 5;
}
