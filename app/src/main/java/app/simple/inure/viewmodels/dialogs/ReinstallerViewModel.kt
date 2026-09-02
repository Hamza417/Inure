package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReinstallerViewModel(application: Application, val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

    private val success: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            initializeCoreFramework()
        }
    }

    fun getSuccessStatus(): LiveData<String> {
        return success
    }

    private fun runCommand() {
        Shell.cmd(getReinstallCommand()).submit { shellResult ->
            if (shellResult.isSuccess) {
                success.postValue("Done")
            } else {
                if (shellResult.err.contains(ERR_3001) || shellResult.out.contains(ERR_3001)) {
                    Shell.cmd(getInstallExistingCommand()).exec().let { result ->
                        if (result.isSuccess) {
                            success.postValue("Done")
                        } else {
                            success.postValue("Failed")
                        }
                    }
                } else {
                    success.postValue("Failed")
                }
            }
        }
    }

    @Suppress("CanConvertToMultiDollarString", "CanUnescapeDollarLiteral")
    private fun getReinstallCommand(): String {
        val appInfo = packageInfo.safeApplicationInfo
        val baseApk = appInfo.sourceDir
        val splits = appInfo.splitSourceDirs

        return if (splits.isNullOrEmpty()) {
            // Fallback for standard single-APK apps
            "pm install -r --user ${getCurrentUser()} \"$baseApk\""
        } else {
            // Handle App Bundles (Split APKs) by creating an installation session
            buildString {
                append("SESSION=\$(pm install-create -r --user ${getCurrentUser()} | grep -oE '[0-9]+'); ")

                append("pm install-write \$SESSION base \"$baseApk\"; ")

                splits.forEachIndexed { index, splitPath ->
                    append("pm install-write \$SESSION split_${index} \"$splitPath\"; ")
                }

                append("pm install-commit \$SESSION")
            }
        }
    }

    private fun getInstallExistingCommand(): String {
        return "pm install-existing --user ${getCurrentUser()} ${packageInfo.packageName}"
    }

    override fun onShellCreated(shell: Shell?) {
        runCommand()
    }

    override fun onShellDenied() {

    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                // Use the list-based 'execute' function to pass the command directly to the Android shell.
                // This prevents Java's Runtime.exec(String) from aggressively splitting our command
                // by spaces, which would break the session variables and split APK paths.
                shizukuServiceHelper.service!!.execute(mutableListOf("sh", "-c", getReinstallCommand()), null, null).let { shellResult ->
                    if (shellResult.isSuccess) {
                        success.postValue("Done")
                    } else {
                        // Installation failed. Check if the OS rejected it due to a build type mismatch
                        // (e.g., trying to install a test-only APK over a production one).
                        val isBuildTypeMismatch = shellResult.error?.contains(ERR_3001) == true ||
                                shellResult.output?.contains(ERR_3001) == true
                        if (isBuildTypeMismatch) {
                            // Fallback: Attempt to install the package that already exists on the device
                            // for the current user. Since this doesn't use complex bash variables,
                            // simpleExecute is safe to use here.
                            shizukuServiceHelper.service!!.simpleExecute(getInstallExistingCommand()).let { fallbackResult ->
                                if (fallbackResult.isSuccess) {
                                    success.postValue("Done")
                                } else {
                                    // Both the reinstallation and fallback attempts failed
                                    success.postValue("Failed")
                                    postWarning(fallbackResult.error + "\n" + fallbackResult.output)
                                }
                            }
                        } else {
                            // The installation failed for a reason other than a build type mismatch
                            success.postValue("Failed")
                            postWarning(shellResult.error + "\n" + shellResult.output)
                        }
                    }
                }
            }.onFailure { exception ->
                success.postValue("Failed")
                postError(exception)
            }.getOrElse { exception ->
                success.postValue("Failed")
                postError(exception)
            }
        }
    }

    companion object {
        private const val ERR_3001 = "INSTALL_FAILED_REJECTED_BY_BUILDTYPE"
    }
}
