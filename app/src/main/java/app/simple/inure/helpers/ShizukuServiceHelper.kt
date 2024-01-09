package app.simple.inure.helpers

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import app.simple.inure.BuildConfig
import app.simple.inure.IUserService
import app.simple.inure.services.ShizukuService
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs

/**
 * 用于简化 Shizuku 绑定、解绑的过程。
 * */
class ShizukuServiceHelper {

    private var _service: IUserService? = null
    val service get() = _service
    val isServiceBound get() = _service != null
    val onServiceConnectedListeners = mutableListOf<Runnable>()

    private val userServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            binder.pingBinder().let {
                _service = if (it) IUserService.Stub.asInterface(binder) else null
                if (!it) {
                    Log.e(TAG, "onServiceConnected: invalid binder for $componentName received")
                }
            }
            onServiceConnectedListeners.forEach { it.run() }
            onServiceConnectedListeners.clear()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            _service = null
        }
    }

    private val userServiceArgs =
        UserServiceArgs(ComponentName(BuildConfig.APPLICATION_ID, ShizukuService::class.java.name))
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

    fun unbindUserService() {
        if (!isServiceBound) return
        if (isSupported()) {
            Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true)
        } else {
            throw RuntimeException("Current Shizuku version is not supported: ${Shizuku.getVersion()}")
        }
    }

    companion object {
        private const val TAG = "ShizukuHelper"

        fun isSupported(): Boolean {
            return Shizuku.getVersion() >= 10
        }
    }
}