package app.simple.inure.extensions.viewmodels

import android.app.Application
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.Warnings
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import com.topjohnwu.superuser.NoShellException
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import rikka.shizuku.Shizuku

abstract class RootShizukuViewModel(application: Application) : WrappedViewModel(application) {

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

                    withTimeout(1) {
                        Shell.enableVerboseLogging = DevelopmentPreferences.get(DevelopmentPreferences.debugMode)

                        kotlin.runCatching {
                            Shell.setDefaultBuilder(
                                    Shell.Builder
                                        .create()
                                        .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                        .setTimeout(10))
                        }.getOrElse {
                            /**
                             * Block crashed deliberately, shell already created
                             * get the traces and ignore the warning
                             */
                            // it.printStackTrace()
                        }

                        Log.d("RootViewModel", "Shell initialization begins")

                        Shell.cmd("su --mount-master").exec().let {
                            if (it.isSuccess) {
                                Log.d("RootViewModel", "Shell initialization successful")
                                shell = Shell.getShell()
                                onShellCreated(shell)
                            } else {
                                Log.d("RootViewModel", "Shell initialization failed")
                                onShellDenied()
                                warning.postValue(Warnings.getNoRootConnectionWarning())
                            }
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
            onShizukuCreated()
        } else {
            onShizukuDenied()
        }
    }

    override fun onCleared() {
        super.onCleared()
        shell?.close()
    }

    @Suppress("unused")
    protected fun getShell(): Shell? {
        return shell
    }

    protected fun isShellAvailable(): Boolean {
        return shell != null
    }

    abstract fun onShellCreated(shell: Shell?)
    abstract fun onShellDenied()
    abstract fun onShizukuCreated()
    open fun onShizukuDenied() {
        warning.postValue(Warnings.getShizukuFailedWarning())
    }
}