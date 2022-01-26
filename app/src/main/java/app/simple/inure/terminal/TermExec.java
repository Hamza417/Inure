package app.simple.inure.terminal;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Looper;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Utility methods for creating and managing a subprocess. This class differs from
 * {@link ProcessBuilder} in that a pty is used to communicate with the subprocess.
 * <p>
 * Pseudo-terminals are a powerful Unix feature, that allows programs to interact with other programs
 * they start in slightly more human-like way. For example, a pty owner can send ^C (aka SIGINT)
 * to attached shell, even if said shell runs under a different user ID.
 */
public class TermExec {
    // Warning: bump the library revision, when an incompatible change happens
    static {
        System.loadLibrary("inure_terminal_emulator");
    }
    
    public static final String SERVICE_ACTION_V1 = "jackpal.androidterm.action.START_TERM.v1";
    
    private static Field descriptorField;
    
    private final List <String> command;
    private final Map <String, String> environment;
    
    public TermExec(@NonNull String... command) {
        this(new ArrayList <>(Arrays.asList(command)));
    }
    
    public TermExec(@NonNull List <String> command) {
        this.command = command;
        this.environment = new Hashtable <>(System.getenv());
    }
    
    public @NonNull
    List <String> command() {
        return command;
    }
    
    public @NonNull
    Map <String, String> environment() {
        return environment;
    }
    
    public @NonNull
    TermExec command(@NonNull String... command) {
        return command(new ArrayList <>(Arrays.asList(command)));
    }
    
    public @NonNull
    TermExec command(List <String> command) {
        command.clear();
        command.addAll(command);
        return this;
    }
    
    /**
     * Start the process and attach it to the pty, corresponding to given file descriptor.
     * You have to obtain this file descriptor yourself by calling
     * {@link ParcelFileDescriptor#open} on special terminal multiplexer
     * device (located at /dev/ptmx).
     * <p>
     * Callers are responsible for closing the descriptor.
     *
     * @return the PID of the started process.
     */
    public int start(@NonNull ParcelFileDescriptor ptmxFd) throws IOException {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new IllegalStateException("This method must not be called from the main thread!");
        }
        
        if (command.size() == 0) {
            throw new IllegalStateException("Empty command!");
        }
        
        final String cmd = command.remove(0);
        final String[] cmdArray = command.toArray(new String[command.size()]);
        final String[] envArray = new String[environment.size()];
        int i = 0;
        for (Map.Entry <String, String> entry : environment.entrySet()) {
            envArray[i++] = entry.getKey() + "=" + entry.getValue();
        }
        
        return createSubprocess(ptmxFd, cmd, cmdArray, envArray);
    }
    
    /**
     * Causes the calling thread to wait for the process associated with the
     * receiver to finish executing.
     *
     * @return The exit value of the Process being waited on
     */
    public static native int waitFor(int processId);
    
    /**
     * Send signal via the "kill" system call. Android {@link android.os.Process#sendSignal} does not
     * allow negative numbers (denoting process groups) to be used.
     */
    public static native void sendSignal(int processId, int signal);
    
    static int createSubprocess(ParcelFileDescriptor masterFd, String cmd, String[] args, String[] envVars) throws IOException {
        final int integerFd;
        
        integerFd = FdHelperHoneycomb.getFd(masterFd);
        
        return createSubprocessInternal(cmd, args, envVars, integerFd);
    }
    
    private static native int createSubprocessInternal(String cmd, String[] args, String[] envVars, int masterFd);
}

// prevents runtime errors on old API versions with ruthless verifier
@TargetApi (Build.VERSION_CODES.HONEYCOMB_MR1)
class FdHelperHoneycomb {
    static int getFd(ParcelFileDescriptor descriptor) {
        return descriptor.getFd();
    }
}
