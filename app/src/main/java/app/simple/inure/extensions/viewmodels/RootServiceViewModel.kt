package app.simple.inure.extensions.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import app.simple.inure.libsu.IRootService
import app.simple.inure.services.RootService
import com.topjohnwu.superuser.ipc.RootService.bind
import com.topjohnwu.superuser.ipc.RootService.unbind
import com.topjohnwu.superuser.nio.FileSystemManager

abstract class RootServiceViewModel(application: Application) : WrappedViewModel(application) {

    private val tag = javaClass.simpleName
    private var aidlConnection: AIDLConnection? = null
    private var daemonConnection: AIDLConnection? = null
    private var fileSystemManager: FileSystemManager? = null

    init {
        val intent = Intent(applicationContext(), RootService::class.java)
        bind(intent, AIDLConnection(isDaemon = false))
    }

    abstract fun runRootProcess(fileSystemManager: FileSystemManager?)

    private fun stopService() {
        try {
            unbind(aidlConnection!!)
        } catch (e: java.lang.NullPointerException) {
            Log.e(tag, "Service not bound")
        }
    }

    inner class AIDLConnection(private val isDaemon: Boolean) : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(tag, "AIDL onServiceConnected")
            if (isDaemon) {
                daemonConnection = this
            } else {
                aidlConnection = this
            }
            val ipc: IRootService = IRootService.Stub.asInterface(service)
            try {
                kotlin.runCatching {
                    // It's crashing, don't uncomment
                    // Log.d(tag, "AIDL PID: ${ipc.pid}")
                    // Log.d(tag, "AIDL UUID: ${ipc.uuid}")
                    // Log.d(tag, "AIDL UID: ${ipc.uid}")
                }

                if (!isDaemon) {
                    // Get the remote file system service proxy through AIDL
                    val binder: IBinder = ipc.fileSystemService
                    // Create a fs manager with the binder proxy.
                    // We will use this fs manager in our stress test.
                    fileSystemManager = FileSystemManager.getRemote(binder)
                }
            } catch (e: RemoteException) {
                Log.e(tag, "Remote error", e)
            }

            runRootProcess(fileSystemManager)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(tag, "AIDL onServiceDisconnected")
            if (isDaemon) {
                daemonConnection = null
            } else {
                aidlConnection = null
                fileSystemManager = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopService()
    }
}