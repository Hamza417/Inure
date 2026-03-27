package app.simple.inure.viewmodels.batch

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.apk.installer.InstallerUtils
import app.simple.inure.apk.utils.PackageUtils.getPackageArchiveInfo
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.BatchInstallerInfo
import app.simple.inure.models.BatchInstallerInfo.InstallState
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.InstallerPreferences
import app.simple.inure.singletons.ApplicationUtils
import app.simple.inure.util.FileUtils.escapeSpecialCharactersForUnixPath
import app.simple.inure.util.FileUtils.getLength
import app.simple.inure.util.SDKUtils
import app.simple.inure.util.StringUtils.endsWithAny
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import java.io.File
import app.simple.inure.shizuku.PackageInstaller as ShizukuPackageInstaller

/**
 * ViewModel responsible for sequentially installing a batch of APK files.
 *
 * Supports Root, Shizuku, and PackageManager installation paths, mirroring
 * the logic inside [app.simple.inure.viewmodels.installer.InstallerViewModel].
 * Each APK — including split-APK bundles (`.apks`, `.xapk`, `.apkm`, `.zip`) —
 * is processed one at a time and its [InstallState] is updated live via [installList].
 *
 * @param application The application context.
 * @param paths List of absolute file paths to APK files selected for installation.
 *
 * @author Hamza417
 */
class BatchInstallerViewModel(
        application: Application,
        private val paths: ArrayList<String>
) : RootShizukuViewModel(application) {

    private val tag = "BatchInstallerViewModel"

    /** File extensions that indicate a split-APK bundle requiring extraction. */
    private val splitApkExtensions = arrayOf(".zip", ".apks", ".apkm", ".xapk")

    /**
     * Internal working list owned exclusively by the ViewModel.
     * Only mutated from within install coroutines; never shared with the UI layer.
     * The UI always receives a fresh immutable snapshot via [_installList].
     */
    private val workingList = ArrayList<BatchInstallerInfo>()

    private val _installList = MutableStateFlow<List<BatchInstallerInfo>>(emptyList())

    /** Emits a new immutable snapshot of the installation list on every state change. */
    val installList: StateFlow<List<BatchInstallerInfo>> = _installList.asStateFlow()

    private val _progress = MutableStateFlow("")

    /** Emits progress strings like "2/5\ncom.example.app". */
    val progress: StateFlow<String> = _progress.asStateFlow()

    /** Deferred used to bridge the async PackageManager install callback into a suspend call. */
    private var installDeferred: CompletableDeferred<Boolean>? = null

    /** Receives session status broadcasts forwarded by [app.simple.inure.services.InstallerSessionService]. */
    private val sessionStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)
            Log.d(tag, "Session status received: $status")

            when (status) {
                PackageInstaller.STATUS_SUCCESS -> installDeferred?.complete(true)
                // On older OS versions a user-confirmation dialog may appear even with
                // USER_ACTION_NOT_REQUIRED set. Mark as failed in batch context because
                // there is no interactive UI to handle it per-app.
                PackageInstaller.STATUS_PENDING_USER_ACTION -> installDeferred?.complete(false)
                else -> installDeferred?.complete(false)
            }
        }
    }

    init {
        LocalBroadcastManager.getInstance(applicationContext())
            .registerReceiver(sessionStatusReceiver, IntentFilter(ServiceConstants.actionSessionStatus))

        viewModelScope.launch(Dispatchers.IO) {
            buildInstallList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(applicationContext())
            .unregisterReceiver(sessionStatusReceiver)
    }

    /**
     * Resolves each path in [paths] into a [BatchInstallerInfo].
     *
     * Every APK — plain or bundled — is copied/extracted into a dedicated subdirectory
     * inside `cacheDir/installer_cache/batch_N/` before installation begins. This mirrors
     * what [app.simple.inure.viewmodels.installer.InstallerViewModel] does via `getInstallerDir()`
     * and is required because `pm install-write` runs under `system_server` which cannot read files
     * on FUSE-mounted external storage (SELinux context `u:object_r:fuse:s0`).
     */
    private fun buildInstallList() {
        // Remove cache directories left over from any previous batch run.
        File(applicationContext().cacheDir, "installer_cache")
            .listFiles()
            ?.filter { it.isDirectory && it.name.startsWith("batch_") }
            ?.forEach { it.deleteRecursively() }

        for ((index, path) in paths.withIndex()) {
            val file = File(path)

            runCatching {
                // Each entry gets its own internal-storage subdirectory so there are
                // no filename collisions across multiple bundles in the same batch.
                val entryCache = File(applicationContext().cacheDir, "installer_cache/batch_$index")
                entryCache.mkdirs()

                val apkFiles: ArrayList<File>

                if (file.name.endsWithAny(*splitApkExtensions)) {
                    // Extract directly into internal cache — no second copy step needed.
                    ZipFile(file.path).extractAll(entryCache.absolutePath)

                    apkFiles = (entryCache.listFiles()?.toList() ?: emptyList())
                        .filter { it.name.endsWith(".apk") && !it.isDirectory }
                        .let { ArrayList(it) }

                    Log.d(tag, "Extracted ${apkFiles.size} APK(s) from ${file.name} → ${entryCache.path}")
                } else {
                    val dest = File(entryCache, file.name)
                    file.copyTo(dest, overwrite = true)
                    apkFiles = arrayListOf(dest)

                    Log.d(tag, "Copied ${file.name} → ${dest.path}")
                }

                // Iterate to find the base APK for metadata, mirroring
                // createPackageInfoAndFilterFiles() in InstallerViewModel.
                var appName = file.nameWithoutExtension
                var packageName = file.nameWithoutExtension

                for (apk in apkFiles) {
                    val info = packageManager.getPackageArchiveInfo(apk) ?: continue
                    appName = info.applicationInfo?.loadLabel(packageManager)?.toString()
                        ?: file.nameWithoutExtension
                    packageName = info.packageName
                    break
                }

                workingList.add(BatchInstallerInfo(file, apkFiles, appName, packageName))
            }.onFailure { e ->
                Log.e(tag, "Failed to resolve ${file.name}: ${e.message}")
                workingList.add(
                        BatchInstallerInfo(
                                file,
                                arrayListOf(file),
                                file.nameWithoutExtension,
                                file.nameWithoutExtension
                        )
                )
            }
        }

        // Emit the initial snapshot (all PENDING) before starting installations.
        _installList.value = workingList.toList()

        if (ConfigurationPreferences.isUsingShizuku() || ConfigurationPreferences.isUsingRoot()) {
            initializeCoreFramework()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                runPackageManagerInstalls()
            }
        }
    }

    override fun onShellCreated(shell: Shell?) {
        viewModelScope.launch(Dispatchers.IO) {
            runRootInstalls()
        }
    }

    override fun onShellDenied() {
        viewModelScope.launch(Dispatchers.IO) {
            runPackageManagerInstalls()
        }
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            runShizukuInstalls()
        }
    }

    override fun onShizukuDenied() {
        viewModelScope.launch(Dispatchers.IO) {
            runPackageManagerInstalls()
        }
    }

    /**
     * Installs all APKs sequentially using the root shell.
     * Supports both plain APKs and split-APK bundles via a multi-file session.
     */
    private fun runRootInstalls() {
        workingList.forEachIndexed { index, info ->
            updateState(index, InstallState.INSTALLING)
            _progress.value = "${index + 1}/${workingList.size}\n${info.packageName}"

            runCatching {
                val files = info.apkFiles.ifEmpty { arrayListOf(info.file) }
                val totalSize = files.getLength()
                val sessionId = with(Shell.cmd("${buildInstallCommand()} $totalSize").exec()) {
                    Log.d(tag, "Create session output: $out")
                    out.first().substringAfter("[").substringBefore("]").toInt()
                }

                for (file in files) {
                    if (file.exists() && file.name.endsWith(".apk")) {
                        val size = file.length()
                        val idx = files.indexOf(file)
                        val escapedPath = file.absolutePath.escapeSpecialCharactersForUnixPath()
                        Shell.cmd("pm install-write -S $size $sessionId $idx $escapedPath").exec().also {
                            Log.d(tag, "Write output for ${file.name}: ${it.out}")
                        }
                    }
                }

                val commitResult = Shell.cmd("pm install-commit $sessionId").exec()
                if (commitResult.isSuccess) {
                    updateState(index, InstallState.INSTALLED)
                    Log.d(tag, "Root install success for ${info.packageName}")
                } else {
                    updateState(index, InstallState.FAILED, commitResult.out.joinToString())
                    Log.d(tag, "Root install failed for ${info.packageName}: ${commitResult.out}")
                }
            }.onFailure { e ->
                Log.e(tag, "Root install exception for ${info.packageName}: ${e.message}")
                updateState(index, InstallState.FAILED, e.message)
            }
        }
    }

    /**
     * Installs all APKs sequentially using the Shizuku service.
     * Passes all split-APK files in a single session when applicable.
     */
    private fun runShizukuInstalls() {
        workingList.forEachIndexed { index, info ->
            updateState(index, InstallState.INSTALLING)
            _progress.value = "${index + 1}/${workingList.size}\n${info.packageName}"

            runCatching {
                val files = info.apkFiles.ifEmpty { listOf(info.file) }
                val uris = files.map { file ->
                    FileProvider.getUriForFile(
                            applicationContext(),
                            "${applicationContext().packageName}.provider",
                            file
                    )
                }

                ApplicationUtils.setApplication(getApplication())
                val result = ShizukuPackageInstaller().install(uris, applicationContext())

                if (result.status == PackageInstaller.STATUS_SUCCESS) {
                    updateState(index, InstallState.INSTALLED)
                    Log.d(tag, "Shizuku install success for ${info.packageName}")
                } else {
                    updateState(index, InstallState.FAILED, "Status: ${result.status} – ${result.message}")
                    Log.d(tag, "Shizuku install failed for ${info.packageName}: ${result.message}")
                }
            }.onFailure { e ->
                Log.e(tag, "Shizuku install exception for ${info.packageName}: ${e.message}")
                updateState(index, InstallState.FAILED, e.message)
            }
        }
    }

    /**
     * Installs all APKs sequentially using the standard PackageManager API.
     * Uses a [CompletableDeferred] bridged via [sessionStatusReceiver] to await each
     * session result before proceeding to the next APK. Handles split-APK bundles by
     * writing all extracted files into a single install session.
     */
    private suspend fun runPackageManagerInstalls() {
        workingList.forEachIndexed { index, info ->
            updateState(index, InstallState.INSTALLING)
            _progress.value = "${index + 1}/${workingList.size}\n${info.packageName}"

            installDeferred = CompletableDeferred()

            runCatching {
                val files = info.apkFiles.ifEmpty { arrayListOf(info.file) }
                val sessionParams = InstallerUtils.makeInstallParams(files.getLength())
                val sessionCode = InstallerUtils.createSession(sessionParams, applicationContext())

                for (file in files) {
                    if (file.exists() && file.name.endsWith(".apk")) {
                        InstallerUtils.installWriteSessions(sessionCode, file, applicationContext())
                    }
                }

                InstallerUtils.commitSession(sessionCode, applicationContext())
            }.onFailure { e ->
                Log.e(tag, "PackageManager install setup exception for ${info.packageName}: ${e.message}")
                installDeferred?.complete(false)
            }

            val success = installDeferred!!.await()
            installDeferred = null

            if (success) {
                updateState(index, InstallState.INSTALLED)
                Log.d(tag, "PackageManager install success for ${info.packageName}")
            } else {
                updateState(index, InstallState.FAILED)
                Log.d(tag, "PackageManager install failed for ${info.packageName}")
            }
        }
    }

    /**
     * Updates the [InstallState] of a single entry in [workingList] and immediately
     * emits a new immutable snapshot to [installList].
     *
     * Because [workingList] is owned exclusively by the ViewModel and the flow emits
     * a fresh copy each time, the UI layer always diffs against its own independent list
     * — avoiding the shared-reference aliasing that would make the diff always return false.
     *
     * @param index The position of the entry to update.
     * @param state The new [InstallState] to apply.
     * @param reason Optional failure reason, relevant when [state] is [InstallState.FAILED].
     */
    private fun updateState(index: Int, state: InstallState, reason: String? = null) {
        workingList[index] = workingList[index].copy(installState = state, failureReason = reason)
        _installList.value = workingList.toList()
    }

    /**
     * Builds the `pm install-create` command string with the configured installer options.
     *
     * @return The full shell command prefix ending with ` -S`.
     */
    private fun buildInstallCommand(): String {
        return buildString {
            append("pm install-create")
            append(" -i ${InstallerPreferences.getInstallerPackageName(applicationContext())}")

            val options = listOf(
                    InstallerPreferences.isGrantRuntimePermissions() to "-g",
                    InstallerPreferences.isVersionCodeDowngrade() to "-d",
                    InstallerPreferences.isTestPackages() to "-t",
                    (InstallerPreferences.isBypassLowTargetSdk() && SDKUtils.isUAndAbove()) to "--bypass-low-target-sdk-block",
                    InstallerPreferences.isReplaceExisting() to "-r",
                    InstallerPreferences.isDontKill() to "--dont-kill"
            )

            options.forEach { (condition, flag) ->
                if (condition) append(" $flag")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                append(" --user ${getCurrentUser()}")
            }

            append(" -S")
        }
    }
}
