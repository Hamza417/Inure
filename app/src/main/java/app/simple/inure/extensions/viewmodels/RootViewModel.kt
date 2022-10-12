package app.simple.inure.extensions.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import app.simple.inure.BuildConfig
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ConditionUtils.invert
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class RootViewModel(application: Application) : WrappedViewModel(application), Shell.GetShellCallback {

    private var shell: Shell? = null

    protected fun initShell() {
        viewModelScope.launch(Dispatchers.IO) {
            if (ConfigurationPreferences.isUsingRoot()) {
                if (Shell.getShell().isRoot) {
                    if (Shell.getShell().isAlive.invert()) {
                        Shell.enableVerboseLogging = BuildConfig.DEBUG

                        kotlin.runCatching {
                            Shell.setDefaultBuilder(
                                    Shell.Builder
                                        .create()
                                        .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                        .setTimeout(10))
                        }.getOrElse {
                            it.printStackTrace()
                        }

                        Shell.getShell(this@RootViewModel)
                    } else {
                        Shell.getShell(this@RootViewModel)
                    }
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

    protected fun getShell(): Shell? {
        return shell
    }

    abstract fun onShellCreated(shell: Shell?)
}