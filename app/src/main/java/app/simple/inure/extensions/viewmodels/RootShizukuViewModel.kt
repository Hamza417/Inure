package app.simple.inure.extensions.viewmodels

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.Warnings
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.shizuku.ShizukuUtils
import com.topjohnwu.superuser.NoShellException
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import rikka.shizuku.Shizuku

abstract class RootShizukuViewModel(application: Application) : PackageUtilsViewModel(application) {

    private var shell: Shell? = null

    /**
     * Initialize the shell or shizuku depending on the user's preference.
     * Root is preferred over shizuku.
     */
    protected fun initializeCoreFramework() {
        if (ConfigurationPreferences.isUsingRoot()) {
            initShell()
        } else if (ConfigurationPreferences.isUsingShizuku()) {
            initShizuku()
        }
    }

    private val onBinderReceivedListener = Shizuku.OnBinderReceivedListener {
        if (Shizuku.isPreV11()) {
            postWarning("Shizuku pre-v11 is not supported")
        } else {
            Log.d("RootViewModel", "Shizuku binder received")
        }
    }

    private val onBinderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d("RootViewModel", "Shizuku binder dead")
    }

    init {
        Shizuku.addBinderReceivedListenerSticky(onBinderReceivedListener)
        Shizuku.addBinderDeadListener(onBinderDeadListener)
    }

    @MainThread
    protected fun initShell() {
        viewModelScope.launch(Dispatchers.IO) {
            if (ConfigurationPreferences.isUsingRoot()) {
                kotlin.runCatching {
                    // This is causing issues with the app
                    //                    if (Shell.getShell().isRoot.invert()) {
                    //                        onShellDenied()
                    //                        warning.postValue(Warnings.getInureWarning01())
                    //                        return@launch
                    //                    }

                    withTimeout(10000) {
                        Shell.enableVerboseLogging = DevelopmentPreferences.get(DevelopmentPreferences.debugMode)

                        kotlin.runCatching {
                            Shell.setDefaultBuilder(
                                    Shell.Builder
                                        .create()
                                        .setContext(applicationContext())
                                        .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                        .setTimeout(10))
                        }.getOrElse {
                            // Shell already initialized
                        }

                        if (Shell.getShell().isRoot) { // Check if root is available
                            onShellCreated(shell)
                        } else {
                            throw NoShellException("No root shell available")
                        }
                    }
                }.onFailure {
                    it.printStackTrace()

                    if (it is NoShellException) {
                        /**
                         * Connection could not be established with the system shell
                         * Show the warning to the user
                         */
                        warning.postValue(Warnings.getNoRootConnectionWarning())
                    } else {
                        /**
                         * Some other error occurred
                         * Show the warning to the user
                         */
                        warning.postValue(it.message)
                    }

                    onShellDenied()
                }
            }
        }
    }

    private fun initShizuku() {
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                onShizukuCreated()
            } else {
                onShizukuDenied()
                Log.d("RootViewModel", "Shizuku permission denied")
            }
        } else {
            onShizukuDenied()
        }
    }

    override fun onCleared() {
        super.onCleared()
        shell?.close()

        kotlin.runCatching {
            Shizuku.removeBinderReceivedListener(onBinderReceivedListener)
            Shizuku.removeBinderDeadListener(onBinderDeadListener)
        }
    }

    @Suppress("unused")
    protected fun getShell(): Shell? {
        return shell
    }

    @Suppress("unused")
    protected fun isShellAvailable(): Boolean {
        return shell != null
    }

    open fun onShellCreated(shell: Shell?) {

    }

    open fun onShellDenied() {

    }

    open fun onShizukuCreated() {

    }

    open fun onShizukuDenied() {
        warning.postValue(Warnings.getShizukuFailedWarning())
    }

    protected fun getCurrentUser(): Int {
        kotlin.runCatching {
            var user = 0
            if (ConfigurationPreferences.isUsingRoot()) {
                Shell.cmd("am get-current-user").exec().let { result ->
                    if (result.isSuccess) {
                        user = result.out.joinToString().toInt()
                    }
                }
            } else if (ConfigurationPreferences.isUsingShizuku()) {
                kotlin.runCatching {
                    ShizukuUtils.execInternal(app.simple.inure.shizuku.Shell.Command("am get-current-user"), null)
                }.onSuccess {
                    user = it.out.toInt()
                }.onFailure {
                    postError(it)
                }
            }

            return user
        }.onFailure {
            postError(it)
        }

        return 0
    }
}
