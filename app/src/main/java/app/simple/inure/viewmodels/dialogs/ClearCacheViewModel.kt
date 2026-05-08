package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.getPackageSize
import app.simple.inure.constants.Warnings
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClearCacheViewModel(application: Application, val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

    private val _state: MutableStateFlow<ClearCacheState> = MutableStateFlow(ClearCacheState.Loading)

    val state: StateFlow<ClearCacheState> = _state.asStateFlow()

    init {
        initializeCoreFramework()
    }

    private fun readCacheSize(): Long {
        return packageInfo.getPackageSize(applicationContext()).cacheSize
    }

    private fun runCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val sizeBefore = readCacheSize()
                Log.d(TAG, "Cache before clear: $sizeBefore for ${packageInfo.packageName}")

                Shell.cmd(getCommand()).submit { shellResult ->
                    kotlin.runCatching {
                        for (line in shellResult.out) {
                            if (line.contains("Exception") || line.contains("not exist")) {
                                throw RuntimeException("Shell reported an error: $line")
                            }
                        }
                    }.onSuccess {
                        if (shellResult.isSuccess) {
                            viewModelScope.launch(Dispatchers.Default) {
                                val sizeAfter = readCacheSize()
                                val cleared = (sizeBefore - sizeAfter).coerceAtLeast(0L)
                                Log.d(TAG, "Cache after clear: $sizeAfter, freed: $cleared")

                                withContext(Dispatchers.Main) {
                                    _state.value = ClearCacheState.Done(cleared)
                                }
                            }
                        } else {
                            _state.value = ClearCacheState.Failed
                        }
                    }.getOrElse {
                        it.printStackTrace()
                        /**
                         * Even if the output contained an error keyword, the shell
                         * command itself may have partially succeeded, so we still
                         * check isSuccess before giving up.
                         */
                        if (shellResult.isSuccess) {
                            viewModelScope.launch(Dispatchers.Default) {
                                withContext(Dispatchers.Main) {
                                    val sizeAfter = readCacheSize()
                                    val cleared = (sizeBefore - sizeAfter).coerceAtLeast(0L)
                                    Log.d(TAG, "Cache after clear (with error in output): $sizeAfter, freed: $cleared")

                                    withContext(Dispatchers.Main) {
                                        _state.value = ClearCacheState.Done(cleared)
                                    }
                                }
                            }
                        } else {
                            _state.value = ClearCacheState.Failed
                        }
                    }
                }
            }.onFailure {
                it.printStackTrace()
                _state.value = ClearCacheState.Failed
            }
        }
    }

    private fun runShizuku(shizukuServiceHelper: ShizukuServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                if (shizukuServiceHelper.isRootMode().not()) {
                    postWarning("Shizuku is not running in root mode and root is required to clear app cache.")
                    _state.value = ClearCacheState.Failed
                    return@launch
                }

                val sizeBefore = readCacheSize()
                Log.d(TAG, "Cache before clear (Shizuku): $sizeBefore for ${packageInfo.packageName}")

                val shellResult = shizukuServiceHelper.service?.simpleExecute(getCommand())

                kotlin.runCatching {
                    for (line in shellResult?.output?.lines().orEmpty()) {
                        if (line.contains("Exception") || line.contains("not exist")) {
                            throw RuntimeException("Shizuku reported an error: $line")
                        }
                    }
                }.onSuccess {
                    if (shellResult?.isSuccess == true) {
                        val sizeAfter = readCacheSize()
                        val cleared = (sizeBefore - sizeAfter).coerceAtLeast(0L)
                        Log.d(TAG, "Cache after clear (Shizuku): $sizeAfter, freed: $cleared")
                        _state.value = ClearCacheState.Done(cleared)
                    } else {
                        _state.value = ClearCacheState.Failed
                    }
                }.getOrElse {
                    it.printStackTrace()
                    if (shellResult?.isSuccess == true) {
                        val sizeAfter = readCacheSize()
                        val cleared = (sizeBefore - sizeAfter).coerceAtLeast(0L)
                        _state.value = ClearCacheState.Done(cleared)
                    } else {
                        _state.value = ClearCacheState.Failed
                    }
                }
            }.onFailure {
                it.printStackTrace()
                _state.value = ClearCacheState.Failed
            }
        }
    }

    private fun getCommand(): String {
        return "pm clear --cache-only --user ${getCurrentUser()} ${packageInfo.packageName}"
    }

    override fun onShellCreated(shell: Shell?) {
        runCommand()
    }

    override fun onShellDenied() {
        warning.postValue(Warnings.getNoRootConnectionWarning())
        _state.value = ClearCacheState.Failed
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        runShizuku(shizukuServiceHelper)
    }

    companion object {
        private const val TAG = "ClearCacheViewModel"

        sealed class ClearCacheState {
            data object Loading : ClearCacheState()

            data class Done(val clearedBytes: Long) : ClearCacheState()

            data object Failed : ClearCacheState()
        }
    }
}
