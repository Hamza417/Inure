package app.simple.inure.terminal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.simple.inure.R;
import app.simple.inure.adapters.terminal.AdapterWindows;
import app.simple.inure.decorations.emulatorview.TermSession;
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView;
import app.simple.inure.extension.activities.BaseActivity;
import app.simple.inure.terminal.compat.ActionBarCompat;
import app.simple.inure.terminal.compat.ActivityCompat;
import app.simple.inure.terminal.compat.AndroidCompat;
import app.simple.inure.terminal.util.SessionList;
import app.simple.inure.util.ViewUtils;

public class WindowList extends BaseActivity implements AdapterWindows.Companion.AdapterWindowsCallback {
    
    private CustomVerticalRecyclerView recyclerView;
    
    private SessionList sessions;
    private AdapterWindows adapterWindows;
    private TermService mTermService;
    
    /**
     * View which isn't automatically in the pressed state if its parent is
     * pressed.  This allows the window's entry to be pressed without the close
     * button being triggered.
     * Idea and code shamelessly borrowed from the Android browser's tabs list.
     * <p>
     * Used by layout xml.
     */
    public static class CloseButton extends androidx.appcompat.widget.AppCompatImageView {
        public CloseButton(Context context) {
            super(context);
        }
        
        public CloseButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        
        public CloseButton(Context context, AttributeSet attrs, int style) {
            super(context, attrs, style);
        }
        
        @Override
        public void setPressed(boolean pressed) {
            if (pressed && ((View) getParent()).isPressed()) {
                return;
            }
            super.setPressed(pressed);
        }
    }
    
    private final ServiceConnection mTSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            TermService.TSBinder binder = (TermService.TSBinder) service;
            mTermService = binder.getService();
            populateList();
        }
        
        public void onServiceDisconnected(ComponentName arg0) {
            mTermService = null;
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal_window_list);
        
        recyclerView = findViewById(R.id.windows_list);
        FloatingActionButton fab = findViewById(R.id.new_window_btn);
        
        setResult(RESULT_CANCELED);
        
        // Display up indicator on action bar home button
        if (AndroidCompat.SDK >= 11) {
            ActionBarCompat bar = ActivityCompat.getActionBar(this);
            if (bar != null) {
                bar.setDisplayOptions(ActionBarCompat.DISPLAY_HOME_AS_UP, ActionBarCompat.DISPLAY_HOME_AS_UP);
            }
        }
        
        fab.setOnClickListener(v -> onWindowClicked(-1 /* Create a new window */));
        
        ViewUtils.INSTANCE.addShadow(fab);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Intent TSIntent = new Intent(this, TermService.class);
        if (!bindService(TSIntent, mTSConnection, BIND_AUTO_CREATE)) {
            Log.w(TermDebug.LOG_TAG, "bind to service failed!");
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    
        AdapterWindows adapter = adapterWindows;
        if (sessions != null) {
            sessions.removeCallback(adapter);
            sessions.removeTitleChangedListener(adapter);
        }
        if (adapter != null) {
            adapter.setSessions(null);
        }
        unbindService(mTSConnection);
    }
    
    private void populateList() {
        sessions = mTermService.getSessions();
        AdapterWindows adapter = adapterWindows;
        
        if (adapter == null) {
            adapter = new AdapterWindows(sessions);
            adapter.setOnAdapterWindowsCallbackListener(this);
            recyclerView.setAdapter(adapter);
            adapterWindows = adapter;
        } else {
            adapter.setSessions(sessions);
        }
        sessions.addCallback(adapter);
        sessions.addTitleChangedListener(adapter);
    }
    
    @Override
    public void onWindowClicked(int position) {
        Intent data = new Intent();
        data.putExtra(Term.EXTRA_WINDOW_ID, position);
        setResult(RESULT_OK, data);
        finish();
    }
    
    @Override
    public void onClose(int position) {
        TermSession session = sessions.remove(position);
        if (session != null) {
            session.finish();
            adapterWindows.onUpdate(position);
        }
    }
}