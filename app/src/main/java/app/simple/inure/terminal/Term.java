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

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import app.simple.inure.R;
import app.simple.inure.activities.preferences.PreferenceActivity;
import app.simple.inure.adapters.terminal.AdapterWindows;
import app.simple.inure.decorations.emulatorview.EmulatorView;
import app.simple.inure.decorations.emulatorview.TermSession;
import app.simple.inure.decorations.emulatorview.UpdateCallback;
import app.simple.inure.decorations.emulatorview.compat.ClipboardManagerCompat;
import app.simple.inure.decorations.emulatorview.compat.ClipboardManagerCompatFactory;
import app.simple.inure.decorations.emulatorview.compat.KeycodeConstants;
import app.simple.inure.decorations.ripple.DynamicRippleImageButton;
import app.simple.inure.decorations.ripple.DynamicRippleTextView;
import app.simple.inure.dialogs.terminal.TerminalCloseWindow;
import app.simple.inure.dialogs.terminal.TerminalContextMenu;
import app.simple.inure.dialogs.terminal.TerminalMainMenu;
import app.simple.inure.dialogs.terminal.TerminalSessionTitle;
import app.simple.inure.dialogs.terminal.TerminalSpecialKeys;
import app.simple.inure.extensions.activities.BaseActivity;
import app.simple.inure.popups.terminal.PopupTerminalWindows;
import app.simple.inure.preferences.ShellPreferences;
import app.simple.inure.preferences.TerminalPreferences;
import app.simple.inure.terminal.compat.ActionBarCompat;
import app.simple.inure.terminal.compat.ActivityCompat;
import app.simple.inure.terminal.compat.AndroidCompat;
import app.simple.inure.terminal.util.SessionList;
import app.simple.inure.terminal.util.TermSettings;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.themes.manager.ThemeUtils;
import app.simple.inure.util.NullSafety;

/**
 * A terminal emulator activity.
 */
@SuppressWarnings ("FieldCanBeLocal")
public class Term extends BaseActivity implements UpdateCallback,
                                                  SharedPreferences.OnSharedPreferenceChangeListener,
                                                  ThemeChangedListener {
    private static final String TAG = "Term";
    /**
     * The ViewFlipper which holds the collection of EmulatorView widgets.
     */
    private TermViewFlipper viewFlipper;
    private DynamicRippleImageButton add;
    private DynamicRippleImageButton close;
    private DynamicRippleImageButton options;
    private DynamicRippleTextView currentWindow;
    private PopupTerminalWindows popupTerminalWindows;
    @SuppressWarnings ("unused")
    private ImageView icon;
    private FrameLayout content;
    
    private SessionList termSessions;
    private AdapterWindows adapterWindows;
    private TermSettings settings;
    
    private final static int SELECT_TEXT_ID = 0;
    private final static int COPY_ALL_ID = 1;
    private final static int PASTE_ID = 2;
    private final static int SEND_CONTROL_KEY_ID = 3;
    private final static int SEND_FN_KEY_ID = 4;
    
    private boolean alreadyStarted = false;
    private boolean stopServiceOnFinish = false;
    
    private Intent TSIntent;
    
    public static final int REQUEST_CHOOSE_WINDOW = 1;
    public static final String EXTRA_WINDOW_ID = "inure.terminal.window_id";
    private int onResumeSelectWindow = -1;
    private ComponentName privateAlias;
    
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    // Available on API 12 and later
    private static final int WIFI_MODE_FULL_HIGH_PERF = 3;
    
    private boolean backKeyPressed;
    
    private static final String ACTION_CLOSE = "inure.terminal.close";
    private static final String ACTION_PATH_BROADCAST = "inure.terminal.broadcast.APPEND_TO_PATH";
    private static final String ACTION_PATH_PREPEND_BROADCAST = "inure.terminal.broadcast.PREPEND_TO_PATH";
    private static final String PERMISSION_PATH_BROADCAST = "inure.terminal.permission.APPEND_TO_PATH";
    private static final String PERMISSION_PATH_PREPEND_BROADCAST = "inure.terminal.permission.PREPEND_TO_PATH";
    private int pendingPathBroadcasts = 0;
    
    private BroadcastReceiver closeBroadcastReceiver;
    
    private TermService termService;
    private int actionBarMode = TermSettings.ACTION_BAR_MODE_NONE;
    private BroadcastReceiver pathReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String path = makePathFromBundle(getResultExtras(false));
            if (intent.getAction().equals(ACTION_PATH_PREPEND_BROADCAST)) {
                settings.setPrependPath(path);
            } else {
                settings.setAppendPath(path);
            }
            pendingPathBroadcasts--;
    
            if (pendingPathBroadcasts <= 0 && termService != null) {
                populateViewFlipper();
                populateWindowList();
            }
        }
    };
    
    // Available on API 12 and later
    private static final int FLAG_INCLUDE_STOPPED_PACKAGES = 0x20;
    @SuppressWarnings ("unused")
    private ActionBarCompat mActionBar;
    
    private void populateViewFlipper() {
        if (termService != null) {
            termSessions = termService.getSessions();
            onResumeSelectWindow = termService.getWindowId();
            
            if (termSessions.size() == 0) {
                try {
                    termSessions.add(createTermSession(null));
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to start terminal session", Toast.LENGTH_LONG).show();
                    supportFinishAfterTransition();
                    return;
                }
            }
            
            termSessions.addCallback(this);
            
            for (TermSession session : termSessions) {
                EmulatorView view = createEmulatorView(session);
                viewFlipper.addView(view);
            }
            
            updatePrefs();
            
            if (onResumeSelectWindow >= 0) {
                viewFlipper.setDisplayedChild(onResumeSelectWindow);
                onResumeSelectWindow = -1;
            }
            
            viewFlipper.setOnViewFlipperFlippedListener((childView, index) -> {
                if (adapterWindows != null && !termSessions.isEmpty()) {
                    currentWindow.setText(adapterWindows.getSessionTitle(index, getBaseContext()));
                    termService.setWindowId(index);
                }
            });
            
            viewFlipper.onResume();
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        settings.readPrefs(sharedPreferences);
    }
    
    private ServiceConnection TSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TermDebug.LOG_TAG, "Bound to TermService");
            TermService.TSBinder binder = (TermService.TSBinder) service;
            termService = binder.getService();
            if (pendingPathBroadcasts <= 0) {
                populateViewFlipper();
                populateWindowList();
            }
        }
        
        public void onServiceDisconnected(ComponentName arg0) {
            termService = null;
        }
    };
    
    @SuppressWarnings ("RedundantLabeledSwitchRuleCodeBlock")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        getWindow().setStatusBarColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground());
        
        fullVersionCheck();
        
        privateAlias = new ComponentName(this, RemoteInterface.PRIVACT_ACTIVITY_ALIAS);
        
        if (savedInstanceState == null) {
            onNewIntent(getIntent());
        }
        
        final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        settings = new TermSettings(getResources(), mPrefs);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        
        Intent broadcast = new Intent(ACTION_PATH_BROADCAST);
        broadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        
        pendingPathBroadcasts++;
        sendOrderedBroadcast(broadcast, PERMISSION_PATH_BROADCAST, pathReceiver, null, RESULT_OK, null, null);
        
        broadcast = new Intent(broadcast);
        broadcast.setAction(ACTION_PATH_PREPEND_BROADCAST);
        
        pendingPathBroadcasts++;
        sendOrderedBroadcast(broadcast, PERMISSION_PATH_PREPEND_BROADCAST, pathReceiver, null, RESULT_OK, null, null);
        
        TSIntent = new Intent(this, TermService.class);
        startService(TSIntent);
        
        if (AndroidCompat.SDK >= 11) {
            int actionBarMode = settings.actionBarMode();
            this.actionBarMode = actionBarMode;
            switch (actionBarMode) {
                case TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE ->
                        setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar);
                case TermSettings.ACTION_BAR_MODE_HIDES ->
                        setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
            }
        } else {
            actionBarMode = TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE;
        }
        
        setContentView(R.layout.activity_terminal);
        viewFlipper = findViewById(R.id.view_flipper);
        add = findViewById(R.id.add);
        close = findViewById(R.id.close);
        options = findViewById(R.id.options);
        currentWindow = findViewById(R.id.current_window);
        icon = findViewById(R.id.terminal_icon);
        content = findViewById(android.R.id.content);
        
        content.setBackgroundColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground());
        
        add.setOnClickListener(v -> doCreateNewWindow());
        close.setOnClickListener(v -> confirmCloseWindow());
        options.setOnClickListener(v -> {
            TerminalMainMenu terminalMainMenu = TerminalMainMenu.Companion.newInstance(wakeLock.isHeld(), wifiLock.isHeld());
            
            terminalMainMenu.setOnTerminalMenuCallbacksListener(source -> {
                switch (source) {
                    case 0 -> {
                        startActivityForResult(new Intent(Term.this, WindowList.class), REQUEST_CHOOSE_WINDOW);
                    }
                    case 1 -> {
                        doToggleSoftKeyboard();
                    }
                    case 2 -> {
                        TerminalSpecialKeys.Companion.newInstance()
                                .show(getSupportFragmentManager(), "special_keys");
                    }
                    case 3 -> {
                        doPreferences();
                    }
                    case 4 -> {
                        doResetTerminal();
                        Toast toast = Toast.makeText(getBaseContext(), R.string.reset_toast_notification, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    case 5 -> {
                        doCopyAll();
                    }
                    case 6 -> {
                        doToggleWakeLock();
                    }
                    case 7 -> {
                        doToggleWifiLock();
                    }
                }
            });
            
            terminalMainMenu.show(getSupportFragmentManager(), "terminal_menu");
        });
        
        currentWindow.setOnClickListener(v -> {
            try {
                popupTerminalWindows = new PopupTerminalWindows(v, adapterWindows);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });
        
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Inure Terminal:" + TermDebug.LOG_TAG);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int wifiLockMode = WifiManager.WIFI_MODE_FULL;
        if (AndroidCompat.SDK >= 12) {
            wifiLockMode = WIFI_MODE_FULL_HIGH_PERF;
        }
        
        wifiLock = wm.createWifiLock(wifiLockMode, TermDebug.LOG_TAG);
        mHaveFullHwKeyboard = checkHaveFullHwKeyboard(getResources().getConfiguration());
        
        updatePrefs();
        alreadyStarted = true;
        
        closeBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    if (intent.getAction().equals(ACTION_CLOSE)) {
                        supportFinishAfterTransition();
                    }
                } catch (
                        NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };
    }
    
    private void populateWindowList() {
        if (termSessions != null) {
            int position = viewFlipper.getDisplayedChild();
            
            if (adapterWindows == null) {
                adapterWindows = new AdapterWindows(termSessions);
                currentWindow.setText(adapterWindows.getSessionTitle(position, String.valueOf(position)));
                
                adapterWindows.setOnAdapterWindowsCallbackListener(new AdapterWindows.Companion.AdapterWindowsCallback() {
                    @Override
                    public void onWindowClicked(int position) {
                        if (position != viewFlipper.getDisplayedChild()) {
                            if (position >= viewFlipper.getChildCount()) {
                                viewFlipper.addView(createEmulatorView(termSessions.get(position - 1)));
                            }
                            
                            if (actionBarMode == TermSettings.ACTION_BAR_MODE_HIDES) {
                                mActionBar.hide();
                            }
                            
                            viewFlipper.setDisplayedChild(position);
                            currentWindow.setText(adapterWindows.getSessionTitle(position, getBaseContext()));
                            termService.setWindowId(position);
                        }
                        
                        if (NullSafety.INSTANCE.isNotNull(popupTerminalWindows)) {
                            popupTerminalWindows.dismiss();
                            popupTerminalWindows = null;
                        }
                    }
                    
                    @Override
                    public void onClose(int position) {
                        TermSession session = termSessions.remove(position);
                        if (session != null) {
                            session.finish();
                            adapterWindows.onUpdate(position);
                        }
                    }
                });
            } else {
                adapterWindows.setSessions(termSessions);
                currentWindow.setText(adapterWindows.getSessionTitle(position, getBaseContext()));
            }
            viewFlipper.addCallback(adapterWindows);
            currentWindow.setText(adapterWindows.getSessionTitle(position, getBaseContext()));
        }
    }
    
    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(closeBroadcastReceiver);
        pathReceiver = null;
        
        super.onDestroy();
    
        if (stopServiceOnFinish) {
            stopService(TSIntent);
        }
        
        termService = null;
        TSConnection = null;
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (wifiLock.isHeld()) {
            wifiLock.release();
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
    
    @Override
    protected void onStart() {
        super.onStart();
        ThemeManager.INSTANCE.addListener(this);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(closeBroadcastReceiver, new IntentFilter(ACTION_CLOSE));
        if (!bindService(TSIntent, TSConnection, BIND_AUTO_CREATE)) {
            throw new IllegalStateException("Failed to bind to TermService!");
        }
    }
    
    private TermSession createTermSession(@Nullable String title) throws IOException {
        TermSettings settings = this.settings;
        TermSession session = createTermSession(this, settings, ShellPreferences.INSTANCE.getInitialCommand());
        session.setTitle(title);
        session.setFinishCallback(termService);
        
        return session;
    }
    
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
            if (keyCode == KeyEvent.KEYCODE_BACK && actionBarMode == TermSettings.ACTION_BAR_MODE_HIDES && mActionBar != null && mActionBar.isShowing()) {
                /* We need to intercept the key event before the view sees it,
                   otherwise the view will handle it before we get it */
                onKeyUp(keyCode, event);
                return true;
            } else {
                return false;
            }
        }
    };
    
    private void updatePrefs() {
        mUseKeyboardShortcuts = TerminalPreferences.INSTANCE.getKeyboardShortcutState();
    
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
    
        viewFlipper.updatePrefs(settings);
    
        for (View v : viewFlipper) {
            ((EmulatorView) v).setDensity(metrics);
            ((TermView) v).updatePrefs(settings);
        }
    
        if (termSessions != null) {
            for (TermSession session : termSessions) {
                ((GenericTermSession) session).updatePrefs(settings);
            }
        }
    
        int orientation = settings.getScreenOrientation();
        int o = 0;
        if (orientation == 0) {
            o = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        } else if (orientation == 1) {
            o = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else //noinspection StatementWithEmptyBody
            if (orientation == 2) {
                o = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else {
                /* Shouldn't be happened. */
            }
        setRequestedOrientation(o);
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
    protected void onStop() {
        /*
         * To protect shared transition animation state
         * TODO - Check this later to see if this method is good enough
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isFinishing()) {
            new Instrumentation().callActivityOnSaveInstanceState(this, new Bundle());
        }
    
        super.onStop();
    
        onResumeSelectWindow = viewFlipper.getDisplayedChild();
        viewFlipper.onPause();
        if (termSessions != null) {
            termSessions.removeCallback(this);
    
            if (adapterWindows != null) {
                termSessions.removeCallback(adapterWindows);
                termSessions.removeTitleChangedListener(adapterWindows);
                viewFlipper.removeCallback(adapterWindows);
            }
        }
    
        viewFlipper.removeAllViews();
        unbindService(TSConnection);
        ThemeManager.INSTANCE.removeListener(this);
    }
    
    private void doCreateNewWindow() {
        if (termSessions == null) {
            Log.w(TermDebug.LOG_TAG, "Couldn't create new window because mTermSessions == null");
            return;
        }
    
        TerminalSessionTitle terminalSessionTitle = TerminalSessionTitle.Companion.newInstance();
    
        terminalSessionTitle.setTerminalSessionTitleCallbacks(title -> {
            try {
                TermSession session = createTermSession(title);
    
                termSessions.add(session);
    
                TermView view = createEmulatorView(session);
                view.updatePrefs(settings);
    
                viewFlipper.addView(view);
                viewFlipper.setDisplayedChild(viewFlipper.getChildCount() - 1);
            } catch (IOException e) {
                Toast.makeText(Term.this, "Failed to create a session", Toast.LENGTH_SHORT).show();
            }
        });
    
        terminalSessionTitle.show(getSupportFragmentManager(), "TerminalSessionTitle");
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
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            // Don't repeat action if intent comes from history
            return;
        }
        
        String action = intent.getAction();
        if (TextUtils.isEmpty(action) || !privateAlias.equals(intent.getComponent())) {
            return;
        }
    
        // huge number simply opens new window
        // TODO: add a way to restrict max number of windows per caller (possibly via reusing BoundSession)
        switch (action) {
            case RemoteInterface.PRIVACT_OPEN_NEW_WINDOW ->
                    onResumeSelectWindow = Integer.MAX_VALUE;
            case RemoteInterface.PRIVACT_SWITCH_WINDOW -> {
                int target = intent.getIntExtra(RemoteInterface.PRIVEXTRA_TARGET_WINDOW, -1);
                if (target >= 0) {
                    onResumeSelectWindow = target;
                }
            }
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
    
    @SuppressLint ("GestureBackNavigation")
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (actionBarMode == TermSettings.ACTION_BAR_MODE_HIDES && mActionBar != null && mActionBar.isShowing()) {
                    mActionBar.hide();
                    return true;
                }
                switch (TerminalPreferences.INSTANCE.getBackButtonAction()) {
                    case TermSettings.BACK_KEY_STOPS_SERVICE:
                        stopServiceOnFinish = true;
                    case TermSettings.BACK_KEY_CLOSES_ACTIVITY:
                        supportFinishAfterTransition();
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
    
    private TermSession getCurrentTermSession() {
        SessionList sessions = termSessions;
        if (sessions == null) {
            return null;
        } else {
            return sessions.get(viewFlipper.getDisplayedChild());
        }
    }
    
    private EmulatorView getCurrentEmulatorView() {
        return (EmulatorView) viewFlipper.getCurrentView();
    }
    
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        mHaveFullHwKeyboard = checkHaveFullHwKeyboard(newConfig);
        
        EmulatorView v = (EmulatorView) viewFlipper.getCurrentView();
        if (v != null) {
            v.updateSize(false);
        }
        
        if (adapterWindows != null) {
            // Force Android to redraw the label in the navigation dropdown
            adapterWindows.notifyDateSet();
        }
        
        ThemeUtils.INSTANCE.setAppTheme(getResources());
        ThemeUtils.INSTANCE.setBarColors(getResources(), getWindow());
    }
    
    private void doToggleWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        } else {
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        }
        ActivityCompat.invalidateOptionsMenu(this);
    }
    
    private void doToggleWifiLock() {
        if (wifiLock.isHeld()) {
            wifiLock.release();
        } else {
            wifiLock.acquire();
        }
        ActivityCompat.invalidateOptionsMenu(this);
    }
    
    private boolean checkHaveFullHwKeyboard(Configuration c) {
        return (c.keyboard == Configuration.KEYBOARD_QWERTY) &&
                (c.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        TerminalContextMenu terminalContextMenu = TerminalContextMenu.Companion.newInstance(canPaste());
        
        terminalContextMenu.setOnTerminalContextMenuCallbackListener(source -> {
            switch (source) {
                case SELECT_TEXT_ID ->
                        getCurrentEmulatorView().toggleSelectingText();
                case COPY_ALL_ID ->
                        doCopyAll();
                case PASTE_ID ->
                        doPaste();
                case SEND_CONTROL_KEY_ID ->
                        doSendControlKey();
                case SEND_FN_KEY_ID ->
                        doSendFnKey();
            }
        });
        
        terminalContextMenu.show(getSupportFragmentManager(), "context_menu");
    }
    
    // Called when the list of sessions changes
    public void onUpdate() {
        SessionList sessions = termSessions;
        if (sessions == null) {
            return;
        }
        
        if (sessions.size() == 0) {
            stopServiceOnFinish = true;
            supportFinishAfterTransition();
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
    
    private void confirmCloseWindow() {
        TerminalCloseWindow terminalCloseWindow = TerminalCloseWindow.Companion.newInstance();
        terminalCloseWindow.setOnTerminalDialogCloseListener(this :: doCloseWindow);
        terminalCloseWindow.show(getSupportFragmentManager(), "terminal_close");
    }
    
    private void doCloseWindow() {
        if (termSessions == null) {
            return;
        }
        
        EmulatorView view = getCurrentEmulatorView();
        if (view == null) {
            return;
        }
        TermSession session = termSessions.remove(viewFlipper.getDisplayedChild());
        view.onPause();
        session.finish();
        viewFlipper.removeView(view);
        if (termSessions.size() != 0) {
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
                    onResumeSelectWindow = termSessions.size() - 1;
                }
            } else {
                // Close the activity if user closed all sessions
                // TODO the left path will be invoked when nothing happened, but this Activity was destroyed!
                if (termSessions == null || termSessions.size() == 0) {
                    stopServiceOnFinish = true;
                    supportFinishAfterTransition();
                }
            }
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
            backKeyPressed = true;
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    
    @SuppressWarnings ("unused")
    private void doUIToggle(int x, int y, int width, int height) {
        switch (actionBarMode) {
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
    
    private boolean canPaste() {
        ClipboardManagerCompat clip = ClipboardManagerCompatFactory.getManager(getApplicationContext());
        return clip.hasText();
    }
    
    private void doPreferences() {
        startActivity(new Intent(this, PreferenceActivity.class));
    }
    
    private void doResetTerminal() {
        TermSession session = getCurrentTermSession();
        if (session != null) {
            session.reset();
        }
    }
    
    private void doCopyAll() {
        ClipboardManagerCompat clip = ClipboardManagerCompatFactory.getManager(getApplicationContext());
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
    
    private String formatMessage(int keyId, int disabledKeyId, Resources r, int arrayId, int enabledId, int disabledId, String regex) {
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