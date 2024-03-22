/*
 * Copyright (C) 2012 Steven Luo
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

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import app.simple.inure.decorations.emulatorview.TermSession;
import app.simple.inure.extensions.activities.BaseActivity;
import app.simple.inure.preferences.ShellPreferences;
import app.simple.inure.terminal.util.SessionList;
import app.simple.inure.terminal.util.TermSettings;

public class RemoteInterface extends BaseActivity {
    
    protected static final String PRIVACT_OPEN_NEW_WINDOW = "inure.terminal.private.OPEN_NEW_WINDOW";
    protected static final String PRIVACT_SWITCH_WINDOW = "inure.terminal.private.SWITCH_WINDOW";
    protected static final String PRIVEXTRA_TARGET_WINDOW = "inure.terminal.private.target_window";
    protected static final String PRIVACT_ACTIVITY_ALIAS = "inure.terminal.TermInternal";
    
    private TermSettings termSettings;
    
    private TermService termService;
    private Intent termServiceIntent;
    
    /**
     * Quote a string so it can be used as a parameter in bash and similar shells.
     */
    public static String quoteForBash(String s) {
        StringBuilder builder = new StringBuilder();
        String specialChars = "\"\\$`!";
        builder.append('"');
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (specialChars.indexOf(c) >= 0) {
                builder.append('\\');
            }
            builder.append(c);
        }
        builder.append('"');
        return builder.toString();
    }
    
    protected void handleIntent() {
        TermService service = getTermService();
        
        if (service == null) {
            finish();
            return;
        }
        
        Intent myIntent = getIntent();
        String action = myIntent.getAction();
        if (action.equals(Intent.ACTION_VIEW) || action.equals("org.openintents.action.VIEW_DIRECTORY")) {
            Uri data = myIntent.getData();
            if (data != null) {
                String path = data.getPath();
                if (path != null) {
                    Log.d(TermDebug.LOG_TAG, "Opening path: " + path);
                    String lastSegment = path.substring(path.lastIndexOf("/") + 1);
                    if (lastSegment.contains(".")) {
                        // This is a file
                        String dirPath = path.substring(0, path.lastIndexOf("/"));
                        openNewWindow("cd " + quoteForBash(dirPath));
                    } else {
                        // This is a directory
                        openNewWindow("cd " + quoteForBash(path));
                    }
                } else {
                    showWarning("Cannot open content:// URIs post SDK 25", true);
                }
            }
        } else {
            // Intent sender may not have permissions, ignore any extras
            openNewWindow(null);
        }
        
        finish();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences(this);
        termSettings = new TermSettings(getResources(), prefs);
        
        Intent TSIntent = new Intent(this, TermService.class);
        termServiceIntent = TSIntent;
        startService(TSIntent);
        if (!bindService(TSIntent, terminalServiceConnection, BIND_AUTO_CREATE)) {
            Log.e(TermDebug.LOG_TAG, "bind to service failed!");
            finish();
        }
    }
    
    @Override
    public void finish() {
        ServiceConnection conn = terminalServiceConnection;
        if (conn != null) {
            unbindService(conn);
            
            // Stop the service if no terminal sessions are running
            TermService service = termService;
            if (service != null) {
                SessionList sessions = service.getSessions();
                if (sessions == null || sessions.isEmpty()) {
                    stopService(termServiceIntent);
                }
            }
            
            terminalServiceConnection = null;
            termService = null;
        }
        super.finish();
    }
    
    private ServiceConnection terminalServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            TermService.TSBinder binder = (TermService.TSBinder) service;
            termService = binder.getService();
            Log.d(TermDebug.LOG_TAG, "RemoteInterface connected to service");
            handleIntent();
        }
        
        public void onServiceDisconnected(ComponentName className) {
            termService = null;
        }
    };
    
    protected TermService getTermService() {
        return termService;
    }
    
    protected String openNewWindow(String iInitialCommand) {
        TermService service = getTermService();
        
        String initialCommand = ShellPreferences.INSTANCE.getInitialCommand();
        Log.d(TermDebug.LOG_TAG, "initialCommand: " + initialCommand);
        if (iInitialCommand != null) {
            Log.d(TermDebug.LOG_TAG, "iInitialCommand: " + iInitialCommand);
            if (initialCommand != null) {
                initialCommand += System.lineSeparator() + iInitialCommand;
                Log.d(TermDebug.LOG_TAG, "initialCommand Appended: " + initialCommand);
            } else {
                initialCommand = iInitialCommand;
                Log.d(TermDebug.LOG_TAG, "initialCommand Reset: " + initialCommand);
            }
        }
        
        try {
            TermSession session = Term.createTermSession(this, termSettings, initialCommand);
            
            session.write("echo $TERM\n");
            session.setFinishCallback(service);
            service.getSessions().add(session);
            service.setWindowId(service.getSessions().indexOf(session));
            
            String handle = UUID.randomUUID().toString();
            ((GenericTermSession) session).setHandle(handle);
            
            Intent intent = new Intent(PRIVACT_OPEN_NEW_WINDOW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            
            return handle;
        } catch (IOException | ActivityNotFoundException e) {
            Log.e(TermDebug.LOG_TAG, "Couldn't create new window: " + e.getMessage());
            return null;
        }
    }
    
    protected String appendToWindow(String handle, String iInitialCommand) {
        TermService service = getTermService();
        
        // Find the target window
        SessionList sessions = service.getSessions();
        GenericTermSession target = null;
        int index;
        for (index = 0; index < sessions.size(); ++index) {
            GenericTermSession session = (GenericTermSession) sessions.get(index);
            String h = session.getHandle();
            if (h != null && h.equals(handle)) {
                target = session;
                Log.d(TermDebug.LOG_TAG, "Found target window: " + index);
                break;
            }
        }
        
        Log.e(TermDebug.LOG_TAG, "Target window not found, opening new one");
        
        if (target == null) {
            // Target window not found, open a new one
            return openNewWindow(iInitialCommand);
        }
        
        if (iInitialCommand != null) {
            target.write(iInitialCommand);
            target.write('\r');
        }
        
        Intent intent = new Intent(PRIVACT_SWITCH_WINDOW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PRIVEXTRA_TARGET_WINDOW, index);
        startActivity(intent);
        
        return handle;
    }
    
    private boolean isContentUri(Uri uri) {
        String scheme = uri.getScheme();
        return scheme != null && scheme.equals("content");
    }
}
