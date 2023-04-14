package app.simple.inure.viewmodels.installer

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.installer.InstallerUtils
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.apk.utils.PackageData.getInstallerDir
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.shizuku.Shell.Command
import app.simple.inure.shizuku.ShizukuUtils
import app.simple.inure.util.FileUtils
import app.simple.inure.util.FileUtils.findFile
import app.simple.inure.util.FileUtils.getLength
import com.anggrayudi.storage.file.baseName
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import net.lingala.zip4j.ZipFile
import java.io.File

class InstallerViewModel(application: Application, private val uri: Uri) : RootShizukuViewModel(application) {

    private var listOfFiles: ArrayList<File>? = null

    private val packageInfo: MutableLiveData<PackageInfo> by lazy {
        MutableLiveData<PackageInfo>().also {
            viewModelScope.launch(Dispatchers.Default) {
                prepareInstallation()
            }
        }
    }

    private val files: MutableLiveData<ArrayList<File>> by lazy {
        MutableLiveData<ArrayList<File>>()
    }

    private val success: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun getPackageInfo(): LiveData<PackageInfo> {
        return packageInfo
    }

    fun getFile(): LiveData<ArrayList<File>> {
        return files
    }

    fun getSuccess(): LiveData<Int> {
        return success
    }

    private fun prepareInstallation() {
        kotlin.runCatching {
            clearInstallerCache()
            PackageData.makePackageFolder(applicationContext())

            uri.let { it ->
                val name = DocumentFile.fromSingleUri(applicationContext(), it)!!
                val sourceFile = if (name.name!!.endsWith(".apk")) {
                    applicationContext().getInstallerDir(name.name!!)
                } else {
                    applicationContext().getInstallerDir(name.baseName + ".zip")
                }

                if (!sourceFile.exists()) {
                    contentResolver.openInputStream(it).use {
                        FileUtils.copyStreamToFile(it!!, sourceFile)
                    }
                }

                if (name.name!!.endsWith(".apkm") || name.name!!.endsWith(".apks")) {
                    ZipFile(sourceFile.path).extractAll(sourceFile.path.substringBeforeLast("."))
                    listOfFiles = File(sourceFile.path.substringBeforeLast(".")).listFiles()!!.toList() as ArrayList<File> /* = java.util.ArrayList<java.io.File> */
                    files.postValue(listOfFiles)
                } else if (name.name!!.endsWith("zip")) {
                    // Verify if zip file has only apk files
                    val hasApk = ZipFile(sourceFile.path).fileHeaders.all { it.fileName.endsWith(".apk") }
                    val hasBaseApk = ZipFile(sourceFile.path).fileHeaders.any { it.fileName.endsWith("base.apk") }

                    if (hasApk && hasBaseApk) {
                        ZipFile(sourceFile.path).extractAll(sourceFile.path.substringBeforeLast("."))
                        listOfFiles = File(sourceFile.path.substringBeforeLast(".")).listFiles()!!.toList() as ArrayList<File> /* = java.util.ArrayList<java.io.File> */
                        files.postValue(listOfFiles)
                    } else {
                        postWarning(context.getString(R.string.zip_is_not_valid))
                        return@runCatching
                    }
                } else if (name.name!!.endsWith(".apk")) {
                    listOfFiles = arrayListOf(sourceFile)
                    this.files.postValue(listOfFiles)
                } else {
                    postWarning(context.getString(R.string.invalid_apk_file))
                    return@runCatching
                }

                postPackageInfo()
            }
        }.getOrElse {
            postError(it)
        }
    }

    private fun postPackageInfo() {
        val file = if (listOfFiles!!.size > 1) listOfFiles!!.findFile("base.apk")!! else listOfFiles!![0]
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageArchiveInfo(file.path, PackageManager.PackageInfoFlags.of(0))!!
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(file.path, PackageUtils.flags.toInt())
        }

        ApkFile(file).use {
            info?.applicationInfo?.name = it.apkMeta.label
        }

        packageInfo.postValue(info)
    }

    private fun packageManagerInstall() {
        viewModelScope.launch(Dispatchers.Default) {
            val sessionParams = InstallerUtils.makeInstallParams(files.value!!.getLength())
            val sessionCode = InstallerUtils.createSession(sessionParams, applicationContext())

            for (file in files.value!!) {
                if (file.exists() && file.name.endsWith(".apk")) {
                    InstallerUtils.installWriteSessions(sessionCode, file, applicationContext())
                }
            }

            InstallerUtils.commitSession(sessionCode, applicationContext())
        }
    }

    private fun rootInstall() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val totalSizeOfAllApks = files.value!!.getLength()
                Log.d("Installer", "Total size of all apks: $totalSizeOfAllApks")
                val sessionId = with(Shell.cmd("${installCommand()} $totalSizeOfAllApks").exec()) {
                    Log.d("Installer", "Output: $out")
                    with(out[0]) {
                        substringAfter("[").substringBefore("]").toInt()
                    }
                }
                Log.d("Installer", "Session id: $sessionId")
                for (file in files.value!!) {
                    if (file.exists() && file.name.endsWith(".apk")) {
                        val size = file.length()
                        Log.d("Installer", "Size of ${file.name}: $size")
                        val splitName = file.name.substringBeforeLast(".")
                        Log.d("Installer", "Split name: $splitName")
                        val idx = files.value?.indexOf(file)
                        Log.d("Installer", "Index: $idx")
                        val path = file.absolutePath.replace(" ", "\\ ").replace("(", "\\(").replace(")", "\\)")
                        Log.d("Installer", "Path: $path")

                        Shell.cmd("pm install-write -S $size $sessionId $idx $path").exec().let {
                            Log.d("Installer", "Output: ${it.out}")
                            Log.d("Installer", "Error: ${it.err}")
                        }
                    }
                }
                Shell.cmd("pm install-commit $sessionId").exec()
            }.onSuccess { it ->
                success.postValue((1..50).random())
                it.out.forEach { Log.d("Installer", it) }
            }.onFailure {
                it.printStackTrace()
                postWarning(it.message.toString())
            }.getOrElse {
                it.printStackTrace()
                postWarning(it.message.toString())
            }
        }
    }

    private fun shizukuInstall() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("Installer", "Shizuku install")

            kotlin.runCatching {
                val totalSizeOfAllApks = files.value!!.getLength()
                Log.d("Installer", "Total size of all apks: $totalSizeOfAllApks")
                val sessionId = with(ShizukuUtils.execInternal(Command("pm install-create -S $totalSizeOfAllApks"), null)) {
                    Log.d("Installer", "Output: $out")
                    with(out) {
                        substringAfter("[").substringBefore("]").toInt()
                    }
                }
                Log.d("Installer", "Session id: $sessionId")
                for (file in files.value!!) {
                    if (file.exists() && file.name.endsWith(".apk") && files.value!!.size > 1) {
                        val size = file.length()
                        Log.d("Installer", "Size of ${file.name}: $size")
                        val splitName = file.name.substringBeforeLast(".")
                        Log.d("Installer", "Split name: $splitName")
                        val idx = files.value?.indexOf(file)
                        Log.d("Installer", "Index: $idx")
                        val path = file.absolutePath.replace(" ", "\\ ").replace("(", "\\(").replace(")", "\\)")
                        Log.d("Installer", "Path: $path")

                        // create uri from file
                        val uri = FileProvider.getUriForFile(applicationContext(), "${applicationContext().packageName}.provider", file)

                        if (file.absolutePath.endsWith("base.apk")) {
                            context.contentResolver.openInputStream(uri).use { inputStream ->
                                ShizukuUtils.execInternal(Command("pm install-write -S $size $sessionId base-"), inputStream).let {
                                    Log.d("Installer", "Output: ${it.out}")
                                    Log.d("Installer", "Error: ${it.err}")
                                }
                            }
                        } else {
                            context.contentResolver.openInputStream(uri).use { inputStream ->
                                ShizukuUtils.execInternal(Command("pm install-write -S $size $sessionId $splitName-"), inputStream).let {
                                    Log.d("Installer", "Output: ${it.out}")
                                    Log.d("Installer", "Error: ${it.err}")
                                }
                            }
                        }
                    } else {
                        // Not a split apk
                        val size = file.length()
                        Log.d("Installer", "Size of ${file.name}: $size")
                        val path = file.absolutePath.replace(" ", "\\ ").replace("(", "\\(").replace(")", "\\)")
                        Log.d("Installer", "Path: $path")

                        // create uri from file
                        val uri = FileProvider.getUriForFile(applicationContext(), "${applicationContext().packageName}.provider", file)

                        context.contentResolver.openInputStream(uri).use { inputStream ->
                            ShizukuUtils.execInternal(Command("pm install-write -S $size $sessionId base-"), inputStream).let {
                                Log.d("Installer", "Output: ${it.out}")
                                Log.d("Installer", "Error: ${it.err}")
                            }
                        }
                    }
                }

                ShizukuUtils.execInternal(Command("pm install-commit $sessionId"), null)
            }.onSuccess { it ->
                success.postValue((1..50).random())
                Log.d("Installer", "Output: ${it.out}")
            }.onFailure {
                it.printStackTrace()
                postWarning(it.message.toString())
            }.getOrElse {
                it.printStackTrace()
                postWarning(it.message.toString())
            }
        }
    }

    fun install() {
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

    override fun onShizukuCreated() {
        shizukuInstall()
    }

    override fun onShizukuDenied() {
        super.onShizukuDenied()
        packageManagerInstall()
    }

    private fun installCommand(): String {
        // Check if greater than nougat
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            "pm install-create --user current -S"
        } else {
            "pm install-create -i -S"
        }
    }

    private fun clearInstallerCache() {
        if (File(applicationContext().cacheDir.path + "/installer_cache/").deleteRecursively()) {
            Log.d(javaClass.name, "Installer cache cleared")
        }
    }

    @Suppress("RedundantOverride")
    override fun onCleared() {
        clearInstallerCache()
        super.onCleared()
    }
}