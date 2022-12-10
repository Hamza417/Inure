package app.simple.inure.extensions.viewmodels

import android.app.Application
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.viewModelScope
import app.simple.inure.BuildConfig
import app.simple.inure.preferences.ConfigurationPreferences
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class RootViewModel(application: Application) : WrappedViewModel(application), Shell.GetShellCallback {

    private var shell: Shell? = null

    @MainThread
    protected fun initShell() {
        viewModelScope.launch(Dispatchers.IO) {
            if (ConfigurationPreferences.isUsingRoot()) {
                kotlin.runCatching {
                    Shell.enableVerboseLogging = BuildConfig.DEBUG

                    kotlin.runCatching {
                        Shell.setDefaultBuilder(
                                Shell.Builder
                                    .create()
                                    .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                    .setTimeout(10))
                    }.getOrElse {
                        /**
                         * Block crashed deliberately,
                         * get the traces and ignore the warning
                         */
                        it.printStackTrace()
                    }

                    Shell.getShell(this@RootViewModel)
                }.onFailure {
                    /**
                     * Connection could not be established with the system shell
                     * Show the warning to the user
                     */
                    warning.postValue(it.message)
                }
            }
        }
    }

    override fun onShell(shell: Shell) {
        this.shell = shell
        onShellCreated(shell)
    }

    override fun onCleared() {
        super.onCleared()
        shell?.close()
    }

    @Suppress("unused")
    protected fun getShell(): Shell? {
        return shell
    }

    abstract fun onShellCreated(shell: Shell?)
}