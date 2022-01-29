package app.simple.inure.terminal;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;

import androidx.core.app.NotificationCompat;
import app.simple.inure.R;
import app.simple.inure.decorations.emulatorview.TermSession;
import app.simple.inure.terminal.compat.ServiceForegroundCompat;
import app.simple.inure.terminal.util.SessionList;
import app.simple.inure.terminal.util.TermSettings;
import app.simple.inure.terminal_v1.ITerminal;

public class TermService extends Service implements TermSession.FinishCallback {
    
    private static final int RUNNING_NOTIFICATION = 1;
    private static final String ACTION_CLOSE = "inure.terminal.close";
    private ServiceForegroundCompat serviceForegroundCompat;
    
    private SessionList mTermSessions;
    
    public class TSBinder extends Binder {
        TermService getService() {
            Log.i("TermService", "Activity binding to service");
            return TermService.this;
        }
    }
    
    private final IBinder mTSBinder = new TSBinder();
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            switch (intent.getAction()) {
                case ACTION_CLOSE:
                    stopSelf();
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    
        return Service.START_NOT_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        if (TermExec.SERVICE_ACTION_V1.equals(intent.getAction())) {
            Log.i("TermService", "Outside process called onBind()");
            return new RBinder();
        } else {
            Log.i("TermService", "Activity called onBind()");
            
            return mTSBinder;
        }
    }
    
    @SuppressLint ("ApplySharedPref")
    @Override
    public void onCreate() {
        createNotificationChannel();
        serviceForegroundCompat = new ServiceForegroundCompat(this);
        mTermSessions = new SessionList();
    
        /* Put the service in the foreground. */
        Intent notifyIntent = new Intent(this, Term.class);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "inure_terminal")
                .setContentIntent(pendingIntent)
                .setContentTitle(getText(R.string.terminal))
                .setSmallIcon(R.drawable.ic_terminal_black)
                .addAction(generateAction(R.drawable.ic_close, getString(R.string.close), ACTION_CLOSE))
                .setContentText(getString(R.string.service_notify_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        serviceForegroundCompat.startForeground(RUNNING_NOTIFICATION, notification);
        
        Log.d(TermDebug.LOG_TAG, "TermService started");
    }
    
    @Override
    public void onDestroy() {
        serviceForegroundCompat.stopForeground(true);
        for (TermSession session : mTermSessions) {
            /* Don't automatically remove from list of sessions -- we clear the
             * list below anyway and we could trigger
             * ConcurrentModificationException if we do */
            session.setFinishCallback(null);
            session.finish();
        }
        mTermSessions.clear();
    }
    
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.terminal);
            NotificationChannel channel = new NotificationChannel("inure_terminal", name, NotificationManager.IMPORTANCE_DEFAULT);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private NotificationCompat.Action generateAction(int icon, String title, String action) {
        Intent closeIntent = new Intent(this, TermService.class);
        closeIntent.setAction(action);
        PendingIntent close = PendingIntent.getService(this, 5087846, closeIntent, 0);
        return new NotificationCompat.Action.Builder(icon, title, close).build();
    }
    
    public SessionList getSessions() {
        return mTermSessions;
    }
    
    public void onSessionFinish(TermSession session) {
        mTermSessions.remove(session);
    }
    
    private final class RBinder extends ITerminal.Stub {
        @Override
        public IntentSender startSession(final ParcelFileDescriptor pseudoTerminalMultiplexerFd,
                final ResultReceiver callback) {
            final String sessionHandle = UUID.randomUUID().toString();
            
            // distinct Intent Uri and PendingIntent requestCode must be sufficient to avoid collisions
            final Intent switchIntent = new Intent(RemoteInterface.PRIVACT_OPEN_NEW_WINDOW)
                    .setData(Uri.parse(sessionHandle))
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(RemoteInterface.PRIVEXTRA_TARGET_WINDOW, sessionHandle);
            
            final PendingIntent result = PendingIntent.getActivity(getApplicationContext(), sessionHandle.hashCode(),
                    switchIntent, PendingIntent.FLAG_IMMUTABLE);
            
            final PackageManager pm = getPackageManager();
            final String[] packages = pm.getPackagesForUid(getCallingUid());
            if (packages == null || packages.length == 0) {
                return null;
            }
            
            for (String packageName : packages) {
                try {
                    final PackageInfo pkgInfo = pm.getPackageInfo(packageName, 0);
    
                    final ApplicationInfo appInfo = pkgInfo.applicationInfo;
                    if (appInfo == null) {
                        continue;
                    }
    
                    final CharSequence label = pm.getApplicationLabel(appInfo);
                    
                    if (!TextUtils.isEmpty(label)) {
                        final String niceName = label.toString();
                        
                        new Handler(Looper.getMainLooper()).post(() -> {
                            GenericTermSession session = null;
                            try {
                                final TermSettings settings = new TermSettings(getResources(),
                                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
                                
                                session = new BoundSession(pseudoTerminalMultiplexerFd, settings, niceName);
                                
                                mTermSessions.add(session);
                                
                                session.setHandle(sessionHandle);
                                session.setFinishCallback(new RBinderCleanupCallback(result, callback));
                                session.setTitle("");
                                
                                session.initializeEmulator(80, 24);
                            } catch (Exception whatWentWrong) {
                                Log.e("TermService", "Failed to bootstrap AIDL session: "
                                        + whatWentWrong.getMessage());
    
                                if (session != null) {
                                    session.finish();
                                }
                            }
                        });
                        
                        return result.getIntentSender();
                    }
                } catch (PackageManager.NameNotFoundException ignore) {
                }
            }
            
            return null;
        }
    }
    
    private final class RBinderCleanupCallback implements TermSession.FinishCallback {
        private final PendingIntent result;
        private final ResultReceiver callback;
        
        public RBinderCleanupCallback(PendingIntent result, ResultReceiver callback) {
            this.result = result;
            this.callback = callback;
        }
        
        @Override
        public void onSessionFinish(TermSession session) {
            result.cancel();
            
            callback.send(0, new Bundle());
            
            mTermSessions.remove(session);
        }
    }
}
