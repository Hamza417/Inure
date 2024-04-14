package app.simple.inure.services

import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.util.Log
import app.simple.inure.libsu.IRootService
import com.topjohnwu.superuser.nio.FileSystemManager
import java.util.UUID

class RootService : com.topjohnwu.superuser.ipc.RootService() {

    companion object {
        private const val TAG = "RootService"

        init {
            // Only load the library when this class is loaded in a root process.
            // The classloader will load this class (and call this static block) in the non-root
            // process because we accessed it when constructing the Intent to send.
            // Add this check so we don't unnecessarily load native code that'll never be used.
            if (Process.myUid() == 0) {
                System.loadLibrary("inure_su")
                Log.d(TAG, "Loaded native library")
            } else {
                Log.d(TAG, "Not root process, native library not loaded")
            }
        }

        // Demonstrate we can also run native code via JNI with RootServices
        external fun nativeGetUid(): Int
    }

    internal class RootIPC : IRootService.Stub() {
        private val uuid: String = UUID.randomUUID().toString()

        override fun getPid(): Int {
            return Process.myPid()
        }

        override fun getUid(): Int {
            return nativeGetUid()
        }

        override fun getUUID(): String {
            return uuid
        }

        override fun getFileSystemService(): IBinder {
            return FileSystemManager.getService()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "AIDLService: onCreate")
    }

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
        // This callback will be called when we are reusing a previously started root process
        Log.i(TAG, "AIDLService: onRebind, daemon process reused")
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "AIDLService: onBind")
        return RootIPC()
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.i(TAG, "AIDLService: onUnbind, client process unbound")
        // Return true here so onRebind will be called
        return false
    }

    override fun stopService(name: Intent?): Boolean {
        Log.i(TAG, "AIDLService: stopService")
        return super.stopService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "AIDLService: onDestroy")
    }
}
