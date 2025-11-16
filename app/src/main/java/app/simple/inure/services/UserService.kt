package app.simple.inure.services

import android.app.ActivityThread
import android.content.Context
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.ServiceManager
import android.util.Log
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import app.simple.inure.IAppOpsActiveCallback
import app.simple.inure.IUserService
import app.simple.inure.util.ExecuteResult
import com.android.internal.app.IAppOpsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess

class UserService() : IUserService.Stub() {
    private val systemContext: Context by lazy {
        try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val systemMain = activityThread.getMethod("systemMain")
            val system = systemMain.invoke(null)
            val getSystemContext = activityThread.getMethod("getSystemContext")
            val context = getSystemContext.invoke(system) as Context
            Log.i(TAG, "Got system context: $context")
            context
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get system context", e)
            throw RuntimeException("Failed to get system context", e)
        }
    }

    private val appOpsService: IAppOpsService by lazy {
        val binder: IBinder = ServiceManager.getService(Context.APP_OPS_SERVICE)
        IAppOpsService.Stub.asInterface(binder)
    }

    // Map our callbacks to system callbacks
    private val activeListeners = mutableMapOf<IAppOpsActiveCallback, com.android.internal.app.IAppOpsActiveCallback>()

    @Keep
    constructor(context: Context) : this() {
        Log.i(TAG, "constructor with Context: context=$context")
        // Force initialization of system context
        Log.i(TAG, "Initializing system context...")
        systemContext
    }

    // Create a system callback that forwards to our callback
    private fun createSystemCallback(ourCallback: IAppOpsActiveCallback): com.android.internal.app.IAppOpsActiveCallback {
        return object : com.android.internal.app.IAppOpsActiveCallback.Stub() {
            // Older Android versions (4 parameters)
            override fun opActiveChanged(op: Int, uid: Int, packageName: String?, active: Boolean) {
                try {
                    Log.d(TAG, "System callback (v1): op=$op, uid=$uid, pkg=$packageName, active=$active")
                    ourCallback.onOpActiveChanged(op, uid, packageName, null, active)
                } catch (e: Exception) {
                    Log.e(TAG, "Error forwarding callback", e)
                }
            }

            // Newer Android versions (7 parameters)
            override fun opActiveChanged(op: Int, uid: Int, packageName: String?, attributionTag: String?, active: Boolean, attributionFlags: Int, attributionChainId: Int) {
                try {
                    Log.d(TAG, "System callback (v2): op=$op, uid=$uid, pkg=$packageName, active=$active")
                    ourCallback.onOpActiveChanged(op, uid, packageName, attributionTag, active)
                } catch (e: Exception) {
                    Log.e(TAG, "Error forwarding callback", e)
                }
            }
        }
    }

    override fun destroy() {
        Log.d(TAG, "destroy")
        exitProcess(0)
    }

    override fun exit() {
        Log.d(TAG, "exit")
        destroy()
    }

    override fun execute(cmdarray: MutableList<String>?, envp: MutableList<String>?, dir: String?): ExecuteResult = runBlocking(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec(cmdarray?.toTypedArray(), envp?.toTypedArray(), dir?.let {
            File(it)
        })
        val exitCode = process.waitFor()
        val error = process.errorStream.readBytes().decodeToString()
        val output = process.inputStream.readBytes().decodeToString()
        Log.d(TAG, "output: \n$output")
        return@runBlocking ExecuteResult(exitCode, error, output)
    }

    override fun executeInputStream(cmdarray: MutableList<String>?, envp: MutableList<String>?, dir: String?, inputPipe: ParcelFileDescriptor?): ExecuteResult = runBlocking(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec(cmdarray?.toTypedArray(), envp?.toTypedArray(), dir?.let {
            File(it)
        })
        process.outputStream.write(inputPipe?.let {
            val inputStream = ParcelFileDescriptor.AutoCloseInputStream(it)
            inputStream.readBytes()
        })
        val exitCode = process.waitFor()
        val error = process.errorStream.readBytes().decodeToString()
        val output = process.inputStream.readBytes().decodeToString()
        return@runBlocking ExecuteResult(exitCode, error, output)
    }

    override fun simpleExecute(command: String?): ExecuteResult = runBlocking(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec(command)
        val exitCode = process.waitFor()
        val error = process.errorStream.readBytes().decodeToString()
        val output = process.inputStream.readBytes().decodeToString()
        return@runBlocking ExecuteResult(exitCode, error, output)
    }

    override fun forceStopApp(packageName: String?): Boolean {
        Log.d(TAG, "forceStopApp: $packageName")
        return try {
            Runtime.getRuntime().exec("am force-stop $packageName").waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "forceStopApp: $e")
            false
        }
    }

    override fun install(paths: MutableList<String>?, opt: MutableList<String>?): Boolean {
        Log.d(TAG, "install: paths=$paths, opt=$opt")
        return try {
            Runtime.getRuntime().exec("pm install ${paths?.joinToString(" ")} ${opt?.joinToString(" ")}").waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "install: $e")
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun startWatchingActive(ops: IntArray?, callback: IAppOpsActiveCallback?) {
        if (ops == null || callback == null) {
            Log.e(TAG, "startWatchingActive: ops or callback is null")
            return
        }

        Log.d(TAG, "startWatchingActive: ops=${ops.contentToString()}")

        try {
            // Create a system callback that forwards to our callback
            val systemCallback = createSystemCallback(callback)

            // Store the mapping
            activeListeners[callback] = systemCallback

            // Call IAppOpsService.startWatchingActive with the system callback
            appOpsService.startWatchingActive(ops, systemCallback)

            Log.d(TAG, "startWatchingActive: registered ${ops.size} operations")
        } catch (e: Exception) {
            Log.e(TAG, "startWatchingActive failed", e)
            activeListeners.remove(callback)
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun stopWatchingActive(callback: IAppOpsActiveCallback?) {
        if (callback == null) return

        Log.d(TAG, "stopWatchingActive")

        try {
            val systemCallback = activeListeners.remove(callback)
            if (systemCallback != null) {
                appOpsService.stopWatchingActive(systemCallback)
                Log.d(TAG, "stopWatchingActive: removed listener")
            }
        } catch (e: Exception) {
            Log.e(TAG, "stopWatchingActive failed", e)
        }
    }

    companion object {
        private const val TAG = "UserService"
    }
}
