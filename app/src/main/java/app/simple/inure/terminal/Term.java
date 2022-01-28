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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import app.simple.inure.R;
import app.simple.inure.decorations.emulatorview.EmulatorView;
import app.simple.inure.decorations.emulatorview.TermSession;
import app.simple.inure.decorations.emulatorview.UpdateCallback;
import app.simple.inure.decorations.emulatorview.compat.ClipboardManagerCompat;
import app.simple.inure.decorations.emulatorview.compat.ClipboardManagerCompatFactory;
import app.simple.inure.decorations.emulatorview.compat.KeycodeConstants;
import app.simple.inure.decorations.ripple.DynamicRippleImageButton;
import app.simple.inure.decorations.typeface.TypeFaceTextView;
import app.simple.inure.dialogs.terminal.DialogSpecialKeys;
import app.simple.inure.extension.activities.BaseActivity;
import app.simple.inure.extension.popup.PopupMenuCallback;
import app.simple.inure.popups.app.PopupTerminal;
import app.simple.inure.preferences.TerminalPreferences;
import app.simple.inure.terminal.compat.ActionBarCompat;
import app.simple.inure.terminal.compat.ActivityCompat;
import app.simple.inure.terminal.compat.AndroidCompat;
import app.simple.inure.terminal.util.SessionList;
import app.simple.inure.terminal.util.TermSettings;

/**
 * A terminal emulator activity.
 */

public class Term extends BaseActivity implements UpdateCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * The ViewFlipper which holds the collection of EmulatorView widgets.
     */
    private TermViewFlipper viewFlipper;
    private DynamicRippleImageButton add;
    private DynamicRippleImageButton close;
    private DynamicRippleImageButton options;
    private Spinner windowsList;
    
    private SessionList mTermSessions;
    
    private TermSettings mSettings;
    
    private final static int SELECT_TEXT_ID = 0;
    private final static int COPY_ALL_ID = 1;
    private final static int PASTE_ID = 2;
    private final static int SEND_CONTROL_KEY_ID = 3;
    private final static int SEND_FN_KEY_ID = 4;
    
    private boolean mAlreadyStarted = false;
    private boolean mStopServiceOnFinish = false;
    
    private Intent TSIntent;
    
    public static final int REQUEST_CHOOSE_WINDOW = 1;
    public static final String EXTRA_WINDOW_ID = "inure.terminal.window_id";
    private int onResumeSelectWindow = -1;
    private ComponentName mPrivateAlias;
    
    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;
    // Available on API 12 and later
    private static final int WIFI_MODE_FULL_HIGH_PERF = 3;
    
    private boolean mBackKeyPressed;
    
    private static final String ACTION_PATH_BROADCAST = "inure.terminal.broadcast.APPEND_TO_PATH";
    private static final String ACTION_PATH_PREPEND_BROADCAST = "inure.terminal.broadcast.PREPEND_TO_PATH";
    private static final String PERMISSION_PATH_BROADCAST = "inure.terminal.permission.APPEND_TO_PATH";
    private static final String PERMISSION_PATH_PREPEND_BROADCAST = "inure.terminal.permission.PREPEND_TO_PATH";
    private int mPendingPathBroadcasts = 0;
    
    private final BroadcastReceiver mPathReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String path = makePathFromBundle(getResultExtras(false));
            if (intent.getAction().equals(ACTION_PATH_PREPEND_BROADCAST)) {
                mSettings.setPrependPath(path);
            } else {
                mSettings.setAppendPath(path);
            }
            mPendingPathBroadcasts--;
            
            if (mPendingPathBroadcasts <= 0 && mTermService != null) {
                populateViewFlipper();
                populateWindowList();
            }
        }
    };
    // Available on API 12 and later
    private static final int FLAG_INCLUDE_STOPPED_PACKAGES = 0x20;
    
    private TermService mTermService;
    private ServiceConnection mTSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TermDebug.LOG_TAG, "Bound to TermService");
            TermService.TSBinder binder = (TermService.TSBinder) service;
            mTermService = binder.getService();
            if (mPendingPathBroadcasts <= 0) {
                populateViewFlipper();
                populateWindowList();
            }
        }
        
        public void onServiceDisconnected(ComponentName arg0) {
            mTermService = null;
        }
    };
    
    private ActionBarCompat mActionBar;
    private int mActionBarMode = TermSettings.ACTION_BAR_MODE_NONE;
    
    private WindowListAdapter mWinListAdapter;
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        mSettings.readPrefs(sharedPreferences);
    }
    
    private class WindowListActionBarAdapter extends WindowListAdapter implements UpdateCallback {
        // From android.R.style in API 13
        private static final int TextAppearance_Holo_Widget_ActionBar_Title = 0x01030112;
        
        public WindowListActionBarAdapter(SessionList sessions) {
            super(sessions);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TypeFaceTextView label = new TypeFaceTextView(Term.this);
            String title = getSessionTitle(position, getString(R.string.window_title, position + 1));
            label.setText(title);
            return label;
        }
        
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }
        
        public void onUpdate() {
            notifyDataSetChanged();
            windowsList.setSelection(viewFlipper.getDisplayedChild(), true);
        }
    }
    
    private boolean mHaveFullHwKeyboard = false;
    
    private class EmulatorViewGestureListener extends SimpleOnGestureListener {
        private final EmulatorView view;
        
        public EmulatorViewGestureListener(EmulatorView view) {
            this.view = view;
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Let the EmulatorView handle taps if mouse tracking is active
            if (view.isMouseTrackingActive()) {
                return false;
            }
            
            //Check for link at tap location
            String link = view.getURLat(e.getX(), e.getY());
            if (link != null) {
                execURL(link);
            } else {
                doUIToggle((int) e.getX(), (int) e.getY(), view.getVisibleWidth(), view.getVisibleHeight());
            }
            return true;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float absVelocityX = Math.abs(velocityX);
            float absVelocityY = Math.abs(velocityY);
            if (absVelocityX > Math.max(1000.0f, 2.0 * absVelocityY)) {
                // Assume user wanted side to side movement
                if (velocityX > 0) {
                    // Left to right swipe -- previous window
                    viewFlipper.showPrevious();
                } else {
                    // Right to left swipe -- next window
                    viewFlipper.showNext();
                }
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Should we use keyboard shortcuts?
     */
    private boolean mUseKeyboardShortcuts;
    
    /**
     * Intercepts keys before the view/terminal gets it.
     */
    private final View.OnKeyListener mKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            return backKeyInterceptor(keyCode, event) || keyboardShortcuts(keyCode, event);
        }
        
        /**
         * Keyboard shortcuts (tab management, paste)
         */
        private boolean keyboardShortcuts(int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            if (!mUseKeyboardShortcuts) {
                return false;
            }
            boolean isCtrlPressed = (event.getMetaState() & KeycodeConstants.META_CTRL_ON) != 0;
            boolean isShiftPressed = (event.getMetaState() & KeycodeConstants.META_SHIFT_ON) != 0;
            
            if (keyCode == KeycodeConstants.KEYCODE_TAB && isCtrlPressed) {
                if (isShiftPressed) {
                    viewFlipper.showPrevious();
                } else {
                    viewFlipper.showNext();
                }
                
                return true;
            } else if (keyCode == KeycodeConstants.KEYCODE_N && isCtrlPressed && isShiftPressed) {
                doCreateNewWindow();
                
                return true;
            } else if (keyCode == KeycodeConstants.KEYCODE_V && isCtrlPressed && isShiftPressed) {
                doPaste();
                
                return true;
            } else {
                return false;
            }
        }
    
        /**
         * Make sure the back button always leaves the application.
         */
        private boolean backKeyInterceptor(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES && mActionBar != null && mActionBar.isShowing()) {
                /* We need to intercept the key event before the view sees it,
                   otherwise the view will handle it before we get it */
                onKeyUp(keyCode, event);
                return true;
            } else {
                return false;
            }
        }
    };
    
    private final Handler mHandler = new Handler();
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.v(TermDebug.LOG_TAG, "onCreate");
        
        mPrivateAlias = new ComponentName(this, RemoteInterface.PRIVACT_ACTIVITY_ALIAS);
    
        if (icicle == null) {
            onNewIntent(getIntent());
        }
    
        final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSettings = new TermSettings(getResources(), mPrefs);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
    
        Intent broadcast = new Intent(ACTION_PATH_BROADCAST);
        broadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
    
        mPendingPathBroadcasts++;
        sendOrderedBroadcast(broadcast, PERMISSION_PATH_BROADCAST, mPathReceiver, null, RESULT_OK, null, null);
    
        broadcast = new Intent(broadcast);
        broadcast.setAction(ACTION_PATH_PREPEND_BROADCAST);
    
        mPendingPathBroadcasts++;
        sendOrderedBroadcast(broadcast, PERMISSION_PATH_PREPEND_BROADCAST, mPathReceiver, null, RESULT_OK, null, null);
    
        TSIntent = new Intent(this, TermService.class);
        startService(TSIntent);
        
        if (AndroidCompat.SDK >= 11) {
            int actionBarMode = mSettings.actionBarMode();
            mActionBarMode = actionBarMode;
            switch (actionBarMode) {
                case TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE:
                    setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar);
                    break;
                case TermSettings.ACTION_BAR_MODE_HIDES:
                    setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
                    break;
            }
        } else {
            mActionBarMode = TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE;
        }
    
        setContentView(R.layout.activity_terminal);
        viewFlipper = findViewById(R.id.view_flipper);
        add = findViewById(R.id.add);
        close = findViewById(R.id.close);
        options = findViewById(R.id.options);
        windowsList = findViewById(R.id.windows_list);
    
        add.setOnClickListener(v -> doCreateNewWindow());
        close.setOnClickListener(v -> confirmCloseWindow());
        options.setOnClickListener(v -> new PopupTerminal(v, mWakeLock, mWifiLock).setOnMenuClickListener(new PopupMenuCallback() {
            @Override
            public void onMenuItemClicked(int source) {
                switch (source) {
                    case 0: {
                        startActivityForResult(new Intent(Term.this, WindowList.class), REQUEST_CHOOSE_WINDOW);
                        break;
                    }
                    case 1: {
                        doToggleSoftKeyboard();
                        break;
                    }
                    case 2: {
                        DialogSpecialKeys.Companion.newInstance()
                                .show(getSupportFragmentManager(), "special_keys");
                        break;
                    }
                    case 3: {
                        doPreferences();
                        break;
                    }
                    case 4: {
                        doResetTerminal();
                        Toast toast = Toast.makeText(getBaseContext(), R.string.reset_toast_notification, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        break;
                    }
                    case 5: {
                        doCopyAll();
                        break;
                    }
                    case 6: {
                        doToggleWakeLock();
                        break;
                    }
                    case 7: {
                        doToggleWifiLock();
                        break;
                    }
                }
            }
        }));
    
        windowsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView <?> parent, View view, int position, long id) {
                if (position != viewFlipper.getDisplayedChild()) {
                    if (position >= viewFlipper.getChildCount()) {
                        viewFlipper.addView(createEmulatorView(mTermSessions.get(position)));
                    }
                    viewFlipper.setDisplayedChild(position);
                    if (mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES) {
                        mActionBar.hide();
                    }
                }
            }
        
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            
            }
        });
    
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Inure Terminal:" + TermDebug.LOG_TAG);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int wifiLockMode = WifiManager.WIFI_MODE_FULL;
        if (AndroidCompat.SDK >= 12) {
            wifiLockMode = WIFI_MODE_FULL_HIGH_PERF;
        }
    
        mWifiLock = wm.createWifiLock(wifiLockMode, TermDebug.LOG_TAG);
        mHaveFullHwKeyboard = checkHaveFullHwKeyboard(getResources().getConfiguration());
        
        updatePrefs();
        mAlreadyStarted = true;
    }
    
    private String makePathFromBundle(Bundle extras) {
        if (extras == null || extras.size() == 0) {
            return "";
        }
        
        String[] keys = new String[extras.size()];
        keys = extras.keySet().toArray(keys);
        Collator collator = Collator.getInstance(Locale.US);
        Arrays.sort(keys, collator);
        
        StringBuilder path = new StringBuilder();
        for (String key : keys) {
            String dir = extras.getString(key);
            if (dir != null && !dir.equals("")) {
                path.append(dir);
                path.append(":");
            }
        }
        
        return path.substring(0, path.length() - 1);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        if (!bindService(TSIntent, mTSConnection, BIND_AUTO_CREATE)) {
            throw new IllegalStateException("Failed to bind to TermService!");
        }
    }
    
    private void populateViewFlipper() {
        if (mTermService != null) {
            mTermSessions = mTermService.getSessions();
            
            if (mTermSessions.size() == 0) {
                try {
                    mTermSessions.add(createTermSession());
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to start terminal session", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            
            mTermSessions.addCallback(this);
            
            for (TermSession session : mTermSessions) {
                EmulatorView view = createEmulatorView(session);
                viewFlipper.addView(view);
            }
            
            updatePrefs();
            
            if (onResumeSelectWindow >= 0) {
                viewFlipper.setDisplayedChild(onResumeSelectWindow);
                onResumeSelectWindow = -1;
            }
            viewFlipper.onResume();
        }
    }
    
    private void populateWindowList() {
        if (mTermSessions != null) {
            int position = viewFlipper.getDisplayedChild();
    
            if (mWinListAdapter == null) {
                mWinListAdapter = new WindowListActionBarAdapter(mTermSessions);
                windowsList.setAdapter(mWinListAdapter);
            } else {
                mWinListAdapter.setSessions(mTermSessions);
            }
            viewFlipper.addCallback(mWinListAdapter);
            windowsList.setSelection(position, true);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        
        if (mStopServiceOnFinish) {
            stopService(TSIntent);
        }
        mTermService = null;
        mTSConnection = null;
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }
    
    private void restart() {
        startActivity(getIntent());
        finish();
    }
    
    protected static TermSession createTermSession(Context context, TermSettings settings, String initialCommand) throws IOException {
        GenericTermSession session = new ShellTermSession(settings, initialCommand);
        // XXX We should really be able to fetch this from within TermSession
        session.setProcessExitMessage(context.getString(R.string.close));
        
        return session;
    }
    
    private TermSession createTermSession() throws IOException {
        TermSettings settings = mSettings;
        TermSession session = createTermSession(this, settings, settings.getInitialCommand());
        session.setFinishCallback(mTermService);
        return session;
    }
    
    private TermView createEmulatorView(TermSession session) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        TermView emulatorView = new TermView(this, session, metrics);
        
        emulatorView.setExtGestureListener(new EmulatorViewGestureListener(emulatorView));
        emulatorView.setOnKeyListener(mKeyListener);
        registerForContextMenu(emulatorView);
        
        return emulatorView;
    }
    
    private TermSession getCurrentTermSession() {
        SessionList sessions = mTermSessions;
        if (sessions == null) {
            return null;
        } else {
            return sessions.get(viewFlipper.getDisplayedChild());
        }
    }
    
    private EmulatorView getCurrentEmulatorView() {
        return (EmulatorView) viewFlipper.getCurrentView();
    }
    
    private void updatePrefs() {
        mUseKeyboardShortcuts = TerminalPreferences.INSTANCE.getKeyboardShortcutState();
        
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
    
        viewFlipper.updatePrefs(mSettings);
    
        for (View v : viewFlipper) {
            ((EmulatorView) v).setDensity(metrics);
            ((TermView) v).updatePrefs(mSettings);
        }
        
        if (mTermSessions != null) {
            for (TermSession session : mTermSessions) {
                ((GenericTermSession) session).updatePrefs(mSettings);
            }
        }
        
        int orientation = mSettings.getScreenOrientation();
        int o = 0;
        if (orientation == 0) {
            o = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        } else if (orientation == 1) {
            o = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (orientation == 2) {
            o = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            /* Shouldn't be happened. */
        }
        setRequestedOrientation(o);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        /* Explicitly close the input method
           Otherwise, the soft keyboard could cover up whatever activity takes
           our place */
        final IBinder token = viewFlipper.getWindowToken();
    
        new Thread() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(token, 0);
            }
        }.start();
    }
    
    @Override
    protected void onStop() {
        viewFlipper.onPause();
        if (mTermSessions != null) {
            mTermSessions.removeCallback(this);
            
            if (mWinListAdapter != null) {
                mTermSessions.removeCallback(mWinListAdapter);
                mTermSessions.removeTitleChangedListener(mWinListAdapter);
                viewFlipper.removeCallback(mWinListAdapter);
            }
        }
    
        viewFlipper.removeAllViews();
        
        unbindService(mTSConnection);
        
        super.onStop();
    }
    
    private boolean checkHaveFullHwKeyboard(Configuration c) {
        return (c.keyboard == Configuration.KEYBOARD_QWERTY) &&
                (c.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO);
    }
    
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        mHaveFullHwKeyboard = checkHaveFullHwKeyboard(newConfig);
    
        EmulatorView v = (EmulatorView) viewFlipper.getCurrentView();
        if (v != null) {
            v.updateSize(false);
        }
        
        if (mWinListAdapter != null) {
            // Force Android to redraw the label in the navigation dropdown
            mWinListAdapter.notifyDataSetChanged();
        }
    }
    
    private void doCreateNewWindow() {
        if (mTermSessions == null) {
            Log.w(TermDebug.LOG_TAG, "Couldn't create new window because mTermSessions == null");
            return;
        }
    
        try {
            TermSession session = createTermSession();
        
            mTermSessions.add(session);
        
            TermView view = createEmulatorView(session);
            view.updatePrefs(mSettings);
        
            viewFlipper.addView(view);
            viewFlipper.setDisplayedChild(viewFlipper.getChildCount() - 1);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to create a session", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void confirmCloseWindow() {
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setMessage(R.string.confirm_window_close_message);
        final Runnable closeWindow = new Runnable() {
            public void run() {
                doCloseWindow();
            }
        };
        b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                mHandler.post(closeWindow);
            }
        });
        b.setNegativeButton(android.R.string.no, null);
        b.show();
    }
    
    private void doCloseWindow() {
        if (mTermSessions == null) {
            return;
        }
        
        EmulatorView view = getCurrentEmulatorView();
        if (view == null) {
            return;
        }
        TermSession session = mTermSessions.remove(viewFlipper.getDisplayedChild());
        view.onPause();
        session.finish();
        viewFlipper.removeView(view);
        if (mTermSessions.size() != 0) {
            viewFlipper.showNext();
        }
    }
    
    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == REQUEST_CHOOSE_WINDOW) {
            if (result == RESULT_OK && data != null) {
                int position = data.getIntExtra(EXTRA_WINDOW_ID, -2);
                if (position >= 0) {
                    // Switch windows after session list is in sync, not here
                    onResumeSelectWindow = position;
                } else if (position == -1) {
                    doCreateNewWindow();
                    onResumeSelectWindow = mTermSessions.size() - 1;
                }
            } else {
                // Close the activity if user closed all sessions
                // TODO the left path will be invoked when nothing happened, but this Activity was destroyed!
                if (mTermSessions == null || mTermSessions.size() == 0) {
                    mStopServiceOnFinish = true;
                    finish();
                }
            }
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            // Don't repeat action if intent comes from history
            return;
        }
    
        String action = intent.getAction();
        if (TextUtils.isEmpty(action) || !mPrivateAlias.equals(intent.getComponent())) {
            return;
        }
    
        // huge number simply opens new window
        // TODO: add a way to restrict max number of windows per caller (possibly via reusing BoundSession)
        switch (action) {
            case RemoteInterface.PRIVACT_OPEN_NEW_WINDOW:
                onResumeSelectWindow = Integer.MAX_VALUE;
                break;
            case RemoteInterface.PRIVACT_SWITCH_WINDOW:
                int target = intent.getIntExtra(RemoteInterface.PRIVEXTRA_TARGET_WINDOW, -1);
                if (target >= 0) {
                    onResumeSelectWindow = target;
                }
                break;
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.edit_text);
        menu.add(0, SELECT_TEXT_ID, 0, R.string.select_text);
        menu.add(0, COPY_ALL_ID, 0, R.string.copy_all);
        menu.add(0, PASTE_ID, 0, R.string.paste);
        menu.add(0, SEND_CONTROL_KEY_ID, 0, R.string.send_control_key);
        menu.add(0, SEND_FN_KEY_ID, 0, R.string.send_fn_key);
        if (!canPaste()) {
            menu.getItem(PASTE_ID).setEnabled(false);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SELECT_TEXT_ID:
                getCurrentEmulatorView().toggleSelectingText();
                return true;
            case COPY_ALL_ID:
                doCopyAll();
                return true;
            case PASTE_ID:
                doPaste();
                return true;
            case SEND_CONTROL_KEY_ID:
                doSendControlKey();
                return true;
            case SEND_FN_KEY_ID:
                doSendFnKey();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /* The pre-Eclair default implementation of onKeyDown() would prevent
           our handling of the Back key in onKeyUp() from taking effect, so
           ignore it here */
        if (AndroidCompat.SDK < 5 && keyCode == KeyEvent.KEYCODE_BACK) {
            /* Android pre-Eclair has no key event tracking, and a back key
               down event delivered to an activity above us in the back stack
               could be succeeded by a back key up event to us, so we need to
               keep track of our own back key presses */
            mBackKeyPressed = true;
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES && mActionBar != null && mActionBar.isShowing()) {
                    mActionBar.hide();
                    return true;
                }
                switch (TerminalPreferences.INSTANCE.getBackButtonAction()) {
                    case TermSettings.BACK_KEY_STOPS_SERVICE:
                        mStopServiceOnFinish = true;
                    case TermSettings.BACK_KEY_CLOSES_ACTIVITY:
                        finish();
                        return true;
                    case TermSettings.BACK_KEY_CLOSES_WINDOW:
                        doCloseWindow();
                        return true;
                    default:
                        return false;
                }
            case KeyEvent.KEYCODE_MENU:
                if (mActionBar != null && !mActionBar.isShowing()) {
                    mActionBar.show();
                    return true;
                } else {
                    return super.onKeyUp(keyCode, event);
                }
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
    
    // Called when the list of sessions changes
    public void onUpdate() {
        SessionList sessions = mTermSessions;
        if (sessions == null) {
            return;
        }
    
        if (sessions.size() == 0) {
            mStopServiceOnFinish = true;
            finish();
        } else if (sessions.size() < viewFlipper.getChildCount()) {
            for (int i = 0; i < viewFlipper.getChildCount(); ++i) {
                EmulatorView v = (EmulatorView) viewFlipper.getChildAt(i);
                if (!sessions.contains(v.getTermSession())) {
                    v.onPause();
                    viewFlipper.removeView(v);
                    --i;
                }
            }
        }
    }
    
    private boolean canPaste() {
        ClipboardManagerCompat clip = ClipboardManagerCompatFactory
                .getManager(getApplicationContext());
        return clip.hasText();
    }
    
    private void doPreferences() {
        startActivity(new Intent(this, TermPreferences.class));
    }
    
    private void doResetTerminal() {
        TermSession session = getCurrentTermSession();
        if (session != null) {
            session.reset();
        }
    }
    
    private void doCopyAll() {
        ClipboardManagerCompat clip = ClipboardManagerCompatFactory
                .getManager(getApplicationContext());
        clip.setText(getCurrentTermSession().getTranscriptText().trim());
    }
    
    private void doPaste() {
        if (!canPaste()) {
            return;
        }
        ClipboardManagerCompat clip = ClipboardManagerCompatFactory
                .getManager(getApplicationContext());
        CharSequence paste = clip.getText();
        getCurrentTermSession().write(paste.toString());
    }
    
    private void doSendControlKey() {
        getCurrentEmulatorView().sendControlKey();
    }
    
    private void doSendFnKey() {
        getCurrentEmulatorView().sendFnKey();
    }
    
    private String formatMessage(int keyId, int disabledKeyId,
            Resources r, int arrayId,
            int enabledId,
            int disabledId, String regex) {
        if (keyId == disabledKeyId) {
            return r.getString(disabledId);
        }
        String[] keyNames = r.getStringArray(arrayId);
        String keyName = keyNames[keyId];
        String template = r.getString(enabledId);
        return template.replaceAll(regex, keyName);
    }
    
    private void doToggleSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        
    }
    
    private void doToggleWakeLock() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        } else {
            mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        }
        ActivityCompat.invalidateOptionsMenu(this);
    }
    
    private void doToggleWifiLock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        } else {
            mWifiLock.acquire();
        }
        ActivityCompat.invalidateOptionsMenu(this);
    }
    
    private void doToggleActionBar() {
        ActionBarCompat bar = mActionBar;
        if (bar == null) {
            return;
        }
        if (bar.isShowing()) {
            bar.hide();
        } else {
            bar.show();
        }
    }
    
    private void doUIToggle(int x, int y, int width, int height) {
        switch (mActionBarMode) {
            case TermSettings.ACTION_BAR_MODE_NONE:
                if (AndroidCompat.SDK >= 11 && (mHaveFullHwKeyboard || y < height / 2)) {
                    openOptionsMenu();
                    return;
                } else {
                    doToggleSoftKeyboard();
                }
                break;
            case TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE:
                if (!mHaveFullHwKeyboard) {
                    doToggleSoftKeyboard();
                }
                break;
            case TermSettings.ACTION_BAR_MODE_HIDES:
                if (mHaveFullHwKeyboard || y < height / 2) {
                    doToggleActionBar();
                    return;
                } else {
                    doToggleSoftKeyboard();
                }
                break;
        }
        getCurrentEmulatorView().requestFocus();
    }
    
    /**
     * Send a URL up to Android to be handled by a browser.
     *
     * @param link The URL to be opened.
     */
    private void execURL(String link) {
        Uri webLink = Uri.parse(link);
        Intent openLink = new Intent(Intent.ACTION_VIEW, webLink);
        PackageManager pm = getPackageManager();
        List <ResolveInfo> handlers = pm.queryIntentActivities(openLink, 0);
        if (handlers.size() > 0) {
            startActivity(openLink);
        }
    }
}
