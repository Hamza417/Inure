package app.simple.inure.viewmodels.installer

import android.app.Application
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.installer.InstallerUtils
import app.simple.inure.apk.utils.APKCertificateUtils
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.apk.utils.PackageData.getInstallerDir
import app.simple.inure.apk.utils.PackageUtils.getPackageArchiveInfo
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.User
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.InstallerPreferences
import app.simple.inure.shizuku.PackageInstaller
import app.simple.inure.singletons.ApplicationUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileUtils
import app.simple.inure.util.FileUtils.escapeSpecialCharactersForUnixPath
import app.simple.inure.util.FileUtils.getLength
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.SDKUtils
import app.simple.inure.util.StringUtils.endsWithAny
import com.anggrayudi.storage.file.baseName
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import java.io.File
import java.security.cert.X509Certificate

class InstallerViewModel(application: Application, private val uri: Uri?, val file: File?) : RootShizukuViewModel(application) {

    private var files: ArrayList<File>? = null
    private var splitApkFiles: ArrayList<File>? = null
    private var baseApk: File? = null
    private var user: User? = null
    private val splitApkExtensions = arrayOf(".zip", ".apks", ".apkm", ".xapk")

    private val packageInfo: MutableLiveData<PackageInfo> by lazy {
        MutableLiveData<PackageInfo>().also {
            viewModelScope.launch(Dispatchers.Default) {
                prepare()
            }
        }
    }

    private val baseApkLiveData: MutableLiveData<File> by lazy {
        MutableLiveData<File>()
    }

    private val success: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private val signatureMismatch: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getPackageInfo(): LiveData<PackageInfo> {
        return packageInfo
    }

    fun getFile(): LiveData<File> {
        return baseApkLiveData
    }

    fun getSuccess(): LiveData<Int> {
        return success
    }

    fun getSignatureStatus(): LiveData<Boolean> {
        return signatureMismatch
    }

    private fun prepare() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                extractFiles()
                createPackageInfoAndFilterFiles()
            }.getOrElse {
                it.printStackTrace()
                postWarning(it.message ?: "Unknown error")
            }
        }
    }

    private fun extractFiles() {
        clearInstallerCache()
        PackageData.makePackageFolder(applicationContext())

        when {
            file != null && file.exists() -> {
                when {
                    file.name.endsWithAny(*splitApkExtensions) -> {
                        ZipFile(file.path).extractAll(file.path.substringBeforeLast("."))
                        files = File(file.path.substringBeforeLast("."))
                            .listFiles()!!.toList() as ArrayList<File> /* = java.util.ArrayList<java.io.File> */
                    }
                    file.name.endsWith(".apk") -> {
                        files = arrayListOf(file)
                    }
                }
            }
            else -> {
                uri?.let { it ->
                    val documentFile = DocumentFile.fromSingleUri(applicationContext(), it)!!
                    val sourceFile = if (documentFile.name!!.endsWith(".apk")) {
                        applicationContext().getInstallerDir(documentFile.name!!)
                    } else {
                        applicationContext().getInstallerDir(documentFile.baseName + ".zip")
                    }

                    if (!sourceFile.exists()) {
                        contentResolver.openInputStream(it).use {
                            FileUtils.copyStreamToFile(it!!, sourceFile)
                        }
                    }

                    if (documentFile.name!!.endsWithAny(*splitApkExtensions)) {
                        ZipFile(sourceFile.path).extractAll(sourceFile.path.substringBeforeLast("."))
                        files = File(sourceFile.path.substringBeforeLast("."))
                            .listFiles()!!.toList() as ArrayList<File> /* = java.util.ArrayList<java.io.File> */
                    } else if (documentFile.name!!.endsWith(".apk")) {
                        files = arrayListOf(sourceFile)
                    }
                }
            }
        }

        // Remove any file from the list that is not an apk
        files!!.removeIf {
            val shouldRemove = !it.name.endsWith(".apk") || it.isDirectory
            if (shouldRemove) {
                Log.d(TAG, "Removing file: ${it.name}")
            }
            shouldRemove
        }
    }

    private fun createPackageInfoAndFilterFiles() {
        files!!.filter { it.absolutePath.endsWith(".apk") }

        if (files!!.size > 1) {
            @Suppress("UNCHECKED_CAST")
            splitApkFiles = files!!.clone() as ArrayList<File>
        } else {
            splitApkFiles = arrayListOf()
        }

        var packageInfo: PackageInfo? = null

        /**
         * Find base/master apk
         */
        for (file in files!!) {
            packageInfo = packageManager.getPackageArchiveInfo(file) ?: continue // We ran into split apk, continue until we find base apk
            packageInfo.safeApplicationInfo.sourceDir = file.absolutePath
            packageInfo.safeApplicationInfo.publicSourceDir = file.absolutePath
            packageInfo.safeApplicationInfo.name = packageManager.getApplicationLabel(packageInfo.safeApplicationInfo).toString()
            packageInfo.safeApplicationInfo.splitSourceDirs = files!!.filter { it != baseApk }.map { it.absolutePath }.toTypedArray()
            this.packageInfo.postValue(packageInfo)
            baseApkLiveData.postValue(file)
            baseApk = file
            signatureCheck(packageInfo)

            try {
                splitApkFiles!!.remove(file)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            break
        }

        if (packageInfo.isNull()) {
            throw Exception("ERR: unable to get package info")
        }
    }

    private fun signatureCheck(packageInfo: PackageInfo) {
        kotlin.runCatching {
            val certificates: Array<X509Certificate>? = APKCertificateUtils(file, packageInfo.packageName, applicationContext()).x509Certificates
            val existingSignatures = APKCertificateUtils(null, packageInfo.packageName, applicationContext()).x509Certificates

            outerLoop@ for (cert in certificates!!) {
                for (existingCert in existingSignatures!!) {
                    val sha1 = APKCertificateUtils.getCertificateFingerprint(cert, APKCertificateUtils.SHA256)
                    val existingSha1 = APKCertificateUtils.getCertificateFingerprint(existingCert, APKCertificateUtils.SHA256)

                    if (sha1 == existingSha1) {
                        signatureMismatch.postValue(false)
                        break@outerLoop
                    } else {
                        signatureMismatch.postValue(true)
                    }
                }
            }
        }.getOrElse {
            it.printStackTrace()
        }
    }

    private fun packageManagerInstall() {
        viewModelScope.launch(Dispatchers.Default) {
            val sessionParams = InstallerUtils.makeInstallParams(files!!.getLength())
            val sessionCode = InstallerUtils.createSession(sessionParams, applicationContext())

            for (file in files!!) {
                if (file.exists() && file.name.endsWith(".apk")) {
                    InstallerUtils.installWriteSessions(sessionCode, file, applicationContext())
                }
            }

            InstallerUtils.commitSession(sessionCode, applicationContext())
        }
    }

    private fun rootInstall() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Shell.cmd("run-as ${applicationContext().packageName}").exec()

                val totalSizeOfAllApks = files!!.getLength()
                Log.d(TAG, "Total size of all apks: $totalSizeOfAllApks")
                val sessionId = with(Shell.cmd("${installCommand()} $totalSizeOfAllApks").exec()) {
                    Log.d(TAG, "Output: $out")
                    with(out.first()) {
                        substringAfter("[").substringBefore("]").toInt()
                    }
                }

                Log.d(TAG, "Session id: $sessionId")
                for (file in files!!) {
                    if (file.exists() && file.name.endsWith(".apk")) {
                        val size = file.length()
                        Log.d(TAG, "Size of ${file.name}: $size")
                        val splitName = file.name.substringBeforeLast(".")
                        Log.d(TAG, "Split name: $splitName")
                        val idx = files?.indexOf(file)
                        Log.d(TAG, "Index: $idx")

                        val path = file.absolutePath.escapeSpecialCharactersForUnixPath()
                        Log.d(TAG, "Path: $path")

                        Shell.cmd("pm install-write -S $size $sessionId $idx $path").exec().let {
                            Log.d(TAG, "Output: ${it.out}")
                            Log.d(TAG, "Error: ${it.err}")
                        }
                    }
                }

                Shell.cmd("pm install-commit $sessionId").exec().let { result ->
                    if (result.isSuccess) {
                        Log.d(TAG, "Output: ${result.out}")
                        Log.d(TAG, "Error: ${result.err}")
                        success.postValue((0..50).random())

                        Log.d(TAG, "Setting installer to ${applicationContext().packageName} for ${packageInfo.value!!.packageName}")
                        Shell.cmd("pm set-installer ${packageInfo.value!!.packageName} ${applicationContext().packageName}").exec().let {
                            if (it.isSuccess) {
                                Log.d(TAG, "Installer set to ${applicationContext().packageName} for ${packageInfo.value!!.packageName}")
                            } else {
                                Log.d(TAG, "Unable to set installer to ${applicationContext().packageName} for ${packageInfo.value!!.packageName}")
                                Log.e(TAG, "Output: ${it.out}")
                            }
                        }
                    } else {
                        Log.d(TAG, "Output: ${result.out}")
                        Log.d(TAG, "Error: ${result.err}")
                        postWarning(result.out.joinToString())
                    }
                }
            } catch (e: java.lang.NullPointerException) {
                if (e.message.isNullOrEmpty().invert()) {
                    postWarning(e.message!!)
                } else {
                    postError(e)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                postError(e)
            }
        }
    }

    fun install(user: User? = null) {
        this.user = user

        if (ConfigurationPreferences.isUsingShizuku() || ConfigurationPreferences.isUsingRoot()) {
            initializeCoreFramework()
        } else {
            packageManagerInstall()
        }
    }

    override fun onShellCreated(shell: Shell?) {
        rootInstall()
    }

    override fun onShellDenied() {
        packageManagerInstall()
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "Shizuku install")

            try {
                val uris = files!!.map { file ->
                    FileProvider.getUriForFile(
                            applicationContext(), "${applicationContext().packageName}.provider", file)
                }

                Log.d(TAG, "Installing ${uris.size} apks")
                ApplicationUtils.setApplication(getApplication()) // Should be initialized at application level, not here
                val packageInstaller = PackageInstaller()
                val shizukuInstall = packageInstaller.install(uris, applicationContext())

                if (shizukuInstall.status == android.content.pm.PackageInstaller.STATUS_SUCCESS) {
                    success.postValue((0..50).random())
                } else {
                    postWarning("ERR: ${shizukuInstall.status} : ${shizukuInstall.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                postError(e)
            }
        }
    }

    override fun onShizukuDenied() {
        /**
         * We don't want to show warning here.
         */
        // super.onShizukuDenied()
        packageManagerInstall()
    }

    private fun clearInstallerCache() {
        kotlin.runCatching {
            if (File(applicationContext().cacheDir.path + "/installer_cache/").deleteRecursively()) {
                Log.d(javaClass.name, "Installer cache cleared")
            }
        }
    }

    private fun installCommand(): String {
        return buildString {
            Log.i(TAG, "creating install command...")
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
                append(" --user ${user?.id ?: getCurrentUser()}")
            }

            append(" -S")
        }
    }

    fun installAnyway() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val path = packageInfo.value!!.applicationInfo?.sourceDir?.escapeSpecialCharactersForUnixPath()

                Shell.cmd("run-as ${applicationContext().packageName}").exec()
                Shell.cmd("pm install --bypass-low-target-sdk-block $path").exec().let {
                    if (it.isSuccess) {
                        success.postValue((0..50).random())
                    } else {
                        postWarning(it.err.joinToString())
                        Log.e(TAG, "Error: ${it.err}")
                    }

                    Log.d(TAG, "Output: ${it.out}")
                }
            }.onFailure {
                postWarning(it.message ?: it.stackTraceToString())
            }
        }
    }

    companion object {
        private const val TAG = "InstallerViewModel"
    }
}
