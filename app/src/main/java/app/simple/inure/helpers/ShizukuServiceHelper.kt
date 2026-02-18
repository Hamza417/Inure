package app.simple.inure.helpers

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import app.simple.inure.BuildConfig
import app.simple.inure.IUserService
import app.simple.inure.services.UserService
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs

/**
 * 用于简化 Shizuku 绑定、解绑的过程。
 * */
class ShizukuServiceHelper private constructor() {

    private var _service: IUserService? = null
    val service get() = _service
    private val isServiceBound get() = _service != null
    val onServiceConnectedListeners = mutableSetOf<Runnable>()

    private val userServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            binder.pingBinder().let {
                _service = if (it) IUserService.Stub.asInterface(binder) else null
                if (!it) {
                    Log.e(TAG, "onServiceConnected: invalid binder for $componentName received")
                } else {
                    Log.d(TAG, "onServiceConnected: $componentName")
                }

            }

            onServiceConnectedListeners.forEach { it.run() }
            onServiceConnectedListeners.clear()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d(TAG, "onServiceDisconnected: $componentName")
            _service = null
        }
    }

    private val userServiceArgs =
        UserServiceArgs(ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name))
            .daemon(false)
            .processNameSuffix("service")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)

    fun bindUserService(onBound: (() -> Unit)?) {
        if (isServiceBound) {
            onBound?.invoke()
            return
        }

        if (isSupported()) {
            val runnable = onBound?.let { Runnable { it.invoke() } }
            runCatching {
                runnable?.let {
                    onServiceConnectedListeners.add(it)
                }

                Shizuku.bindUserService(userServiceArgs, userServiceConnection)
            }.onFailure { throwable ->
                runnable?.let {
                    onServiceConnectedListeners.remove(it)
                }

                throwable.printStackTrace()
                throw throwable
            }
        } else {
            throw RuntimeException("Current Shizuku version is not supported: ${Shizuku.getVersion()}")
        }
    }

    fun getBoundService(onServiceConnected: (IUserService) -> Unit) {
        bindUserService {
            _service?.let { onServiceConnected(it) }
        }
    }

    fun unbindUserService() {
        if (!isServiceBound) return
        if (isSupported()) {
            Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true)
        } else {
            throw RuntimeException("Current Shizuku version is not supported: ${Shizuku.getVersion()}")
        }
    }

    fun isRootMode(): Boolean {
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            val uid = Shizuku.getUid()
            if (uid == 0) {
                // Running as Root
                return true
            } else if (uid == 2000) {
                // Running as ADB (Shell)
                return false
            } else {
                // Running as an app with Shizuku permission
                return false
            }
        } else {
            return false
        }
    }

    companion object {
        private const val TAG = "ShizukuHelper"
        private var INSTANCE: ShizukuServiceHelper? = null

        fun getInstance(): ShizukuServiceHelper {
            if (INSTANCE == null) {
                INSTANCE = ShizukuServiceHelper()
            }
            return INSTANCE!!
        }

        fun isSupported(): Boolean {
            return Shizuku.getVersion() >= 10
        }
    }
}
