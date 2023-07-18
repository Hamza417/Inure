package app.simple.inure.extensions.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import app.simple.inure.BuildConfig
import app.simple.inure.libsu.IRootService
import app.simple.inure.services.RootService
import app.simple.inure.util.ConditionUtils.invert
import com.topjohnwu.superuser.ipc.RootService.bind
import com.topjohnwu.superuser.ipc.RootService.stop
import com.topjohnwu.superuser.ipc.RootService.unbind
import com.topjohnwu.superuser.nio.FileSystemManager

abstract class RootServiceViewModel(application: Application) : WrappedViewModel(application) {

    private val tag = "RootService"
    private var aidlConnection: AIDLConnection? = null
    private var daemonConnection: AIDLConnection? = null
    private var fileSystemManager: FileSystemManager? = null

    protected fun initRootProc() {
        Log.d(tag, "Root proc init")
        val intent = Intent(application, RootService::class.java)
        bind(intent, AIDLConnection(isDaemon = false))
    }

    abstract fun runRootProcess(fileSystemManager: FileSystemManager?)

    private fun unbind() {
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

                if (isDaemon.invert()) {
                    // Get the remote file system service proxy through AIDL
                    val binder: IBinder = ipc.fileSystemService
                    // Create a fs manager with the binder proxy.
                    // We will use this fs manager in our stress test.
                    fileSystemManager = FileSystemManager.getRemote(binder)
                    runRootProcess(fileSystemManager)
                }
            } catch (e: RemoteException) {
                postWarning("Failed to get remote service")

                if (BuildConfig.DEBUG) {
                    throw RuntimeException(e)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(tag, "AIDL onServiceDisconnected")
            if (isDaemon) {
                daemonConnection = null
            } else {
                aidlConnection = null
                fileSystemManager = null
            }

            stop(Intent(application, RootService::class.java))
        }
    }

    protected fun getFileSystemManager(): FileSystemManager? {
        return fileSystemManager
    }

    override fun onCleared() {
        super.onCleared()
        unbind()
    }
}