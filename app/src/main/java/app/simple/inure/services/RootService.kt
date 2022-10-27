package app.simple.inure.services

import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.util.Log
import app.simple.inure.libsu.IRootService
import com.topjohnwu.superuser.nio.FileSystemManager
import java.util.*

class RootService : com.topjohnwu.superuser.ipc.RootService() {

    private val tag = javaClass.simpleName
    private val uuid: String = UUID.randomUUID().toString()

    companion object {
        init {
            // Only load the library when this class is loaded in a root process.
            // The classloader will load this class (and call this static block) in the non-root
            // process because we accessed it when constructing the Intent to send.
            // Add this check so we don't unnecessarily load native code that'll never be used.
            if (Process.myUid() == 0) {
                System.loadLibrary("inure_su")
                Log.d("RootService", "Loaded native library")
            }
        }

        // Demonstrate we can also run native code via JNI with RootServices
        external fun nativeGetUid(): Int
    }

    internal class RootIPC : IRootService.Stub() {
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
        Log.d(tag, "AIDLService: onCreate, $uuid")
    }

    override fun onRebind(intent: Intent) {
        // This callback will be called when we are reusing a previously started root process
        Log.d(tag, "AIDLService: onRebind, daemon process reused")
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(tag, "AIDLService: onBind")
        return RootIPC()
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(tag, "AIDLService: onUnbind, client process unbound")
        // Return true here so onRebind will be called
        return true
    }

    override fun onDestroy() {
        Log.d(tag, "AIDLService: onDestroy")
    }
}