/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.simple.inure.terminal;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import app.simple.inure.preferences.ShellPreferences;
import app.simple.inure.shizuku.ShizukuUtils;
import app.simple.inure.terminal.compat.FileCompat;
import app.simple.inure.terminal.util.TermSettings;

/**
 * A terminal session, controlling the process attached to the session (usually
 * a shell). It keeps track of process PID and destroys it's process group
 * upon stopping.
 */
public class ShellTermSession extends GenericTermSession {
    private final Thread watcherThread;
    private final String initialCommand;
    private final Handler msgHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (!isRunning()) {
                return;
            }
            if (msg.what == PROCESS_EXITED) {
                onProcessExit((Integer) msg.obj);
            }
        }
    };
    
    private static final int PROCESS_EXITED = 1;
    private int processId;
    
    public ShellTermSession(TermSettings settings, String initialCommand) throws IOException {
        super(ParcelFileDescriptor.open(new File("/dev/ptmx"), ParcelFileDescriptor.MODE_READ_WRITE),
                settings, false);
        
        initializeSession();
        
        setTermOut(new ParcelFileDescriptor.AutoCloseOutputStream(termParcelFileDescriptor));
        setTermIn(new ParcelFileDescriptor.AutoCloseInputStream(termParcelFileDescriptor));
        
        this.initialCommand = initialCommand;
        
        watcherThread = new Thread() {
            @Override
            public void run() {
                Log.i(TermDebug.LOG_TAG, "waiting for: " + processId);
                int result = TermExec.waitFor(processId);
                Log.i(TermDebug.LOG_TAG, "Subprocess exited: " + result);
                msgHandler.sendMessage(msgHandler.obtainMessage(PROCESS_EXITED, result));
            }
        };
        
        watcherThread.setName("Process watcher");
    }
    
    private void initializeSession() throws IOException {
        TermSettings settings = termSettings;
        
        String path = System.getenv("PATH");
    
        if (ShellPreferences.INSTANCE.getAllowPathExtensionsState()) {
            String appendPath = settings.getAppendPath();
            if (appendPath != null && appendPath.length() > 0) {
                path = path + ":" + appendPath;
            }
        
            if (ShellPreferences.INSTANCE.getAllowPathPrependState()) {
                String prependPath = settings.getPrependPath();
                if (prependPath != null && prependPath.length() > 0) {
                    path = prependPath + ":" + path;
                }
            }
        }
    
        if (ShellPreferences.INSTANCE.getVerifyPathEntriesState()) {
            path = checkPath(path);
        }
    
        String[] env = new String[3];
        env[0] = "TERM=" + ShellPreferences.INSTANCE.getTerminalType();
        env[1] = "PATH=" + path;
        env[2] = "HOME=" + ShellPreferences.INSTANCE.getHomePath();
    
        processId = createSubprocess(ShellPreferences.INSTANCE.getCommandLine(), env);
    }
    
    private String checkPath(String path) {
        String[] dirs = path.split(":");
        StringBuilder checkedPath = new StringBuilder(path.length());
        for (String dirname : dirs) {
            File dir = new File(dirname);
            if (dir.isDirectory() && FileCompat.canExecute(dir)) {
                checkedPath.append(dirname);
                checkedPath.append(":");
            }
        }
        return checkedPath.substring(0, checkedPath.length() - 1);
    }
    
    @Override
    public void initializeEmulator(int columns, int rows) {
        super.initializeEmulator(columns, rows);
        watcherThread.start();
        sendInitialCommand(initialCommand);
    }
    
    private void sendInitialCommand(String initialCommand) {
        /*
         * If the user has enabled RISH, we will send the command to start RISH
         * before sending the initial command. This is because RISH will start
         * a new shell, and we want to make sure that the initial command is sent
         * to the new shell.
         *
         * This maybe the best solution or I may be missing something obvious but
         * for now it works.
         */
        if (ShellPreferences.INSTANCE.isUsingRISH()) {
            write(ShizukuUtils.getRishCommand() + '\r');
        }
    
        if (initialCommand.length() > 0) {
            write(initialCommand + '\r');
        }
    }
    
    private int createSubprocess(String shell, String[] env) throws IOException {
        ArrayList <String> argList = parse(shell);
        String arg0;
        String[] args;
        
        try {
            arg0 = argList.get(0);
            File file = new File(arg0);
            if (!file.exists()) {
                Log.e(TermDebug.LOG_TAG, "Shell " + arg0 + " not found!");
                throw new FileNotFoundException(arg0);
            } else if (!FileCompat.canExecute(file)) {
                Log.e(TermDebug.LOG_TAG, "Shell " + arg0 + " not executable!");
                throw new FileNotFoundException(arg0);
            }
            args = argList.toArray(new String[1]);
        } catch (Exception e) {
            argList = parse(termSettings.getFailsafeShell());
            arg0 = argList.get(0);
            args = argList.toArray(new String[1]);
        }
    
        return TermExec.createSubprocess(termParcelFileDescriptor, arg0, args, env);
    }
    
    private int createShizukuRishSubprocess(String shell, String[] env) throws IOException {
        ArrayList <String> argList = parse(shell);
        String arg0;
        String[] args;
        
        try {
            arg0 = argList.get(0);
            File file = new File(arg0);
            if (!file.exists()) {
                Log.e(TermDebug.LOG_TAG, "Shell " + arg0 + " not found!");
                throw new FileNotFoundException(arg0);
            } else if (!FileCompat.canExecute(file)) {
                Log.e(TermDebug.LOG_TAG, "Shell " + arg0 + " not executable!");
                throw new FileNotFoundException(arg0);
            }
            args = argList.toArray(new String[1]);
        } catch (Exception e) {
            argList = parse(termSettings.getFailsafeShell());
            arg0 = argList.get(0);
            args = argList.toArray(new String[1]);
        }
        
        return TermExec.createSubprocess(termParcelFileDescriptor, arg0, args, env);
    }
    
    private ArrayList <String> parse(String cmd) {
        final int PLAIN = 0;
        final int WHITESPACE = 1;
        final int IN_QUOTE = 2;
        int state = WHITESPACE;
        ArrayList <String> result = new ArrayList <>();
        int cmdLen = cmd.length();
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < cmdLen; i++) {
            char c = cmd.charAt(i);
            if (state == PLAIN) {
                if (Character.isWhitespace(c)) {
                    result.add(builder.toString());
                    builder.delete(0, builder.length());
                    state = WHITESPACE;
                } else if (c == '"') {
                    state = IN_QUOTE;
                } else {
                    builder.append(c);
                }
            } else if (state == WHITESPACE) {
                //noinspection StatementWithEmptyBody
                if (Character.isWhitespace(c)) {
                    // do nothing
                } else if (c == '"') {
                    state = IN_QUOTE;
                } else {
                    state = PLAIN;
                    builder.append(c);
                }
            } else if (state == IN_QUOTE) {
                if (c == '\\') {
                    if (i + 1 < cmdLen) {
                        i += 1;
                        builder.append(cmd.charAt(i));
                    }
                } else if (c == '"') {
                    state = PLAIN;
                } else {
                    builder.append(c);
                }
            }
        }
        
        if (builder.length() > 0) {
            result.add(builder.toString());
        }
        
        return result;
    }
    
    private void onProcessExit(@SuppressWarnings ("unused") int result) {
        onProcessExit();
    }
    
    @Override
    public void finish() {
        hangupProcessGroup();
        super.finish();
    }
    
    /**
     * Send SIGHUP to a process group, SIGHUP notifies a terminal client, that the terminal have been disconnected,
     * and usually results in client's death, unless it's process is a daemon or have been somehow else detached
     * from the terminal (for example, by the "nohup" utility).
     */
    void hangupProcessGroup() {
        TermExec.sendSignal(-processId, 1);
    }
}
