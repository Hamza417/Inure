package app.simple.inure.viewmodels.installer

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.installer.InstallerUtils
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.apk.utils.PackageData.getInstallerDir
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.FileUtils
import app.simple.inure.util.FileUtils.findFile
import app.simple.inure.util.FileUtils.getLength
import com.anggrayudi.storage.file.baseName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import net.lingala.zip4j.ZipFile
import java.io.File

class InstallerViewModel(application: Application, private val uri: Uri) : WrappedViewModel(application) {

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

    fun getPackageInfo(): LiveData<PackageInfo> {
        return packageInfo
    }

    fun getFile(): LiveData<ArrayList<File>> {
        return files
    }

    private fun prepareInstallation() {
        kotlin.runCatching {
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
                        postWarning("Zip file is not a valid split apk")
                        return@runCatching
                    }
                } else if (name.name!!.endsWith(".apk")) {
                    listOfFiles = arrayListOf(sourceFile)
                    this.files.postValue(listOfFiles)
                } else {
                    postWarning("File is not a valid apk")
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

    fun install() {
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

    @Suppress("RedundantOverride")
    override fun onCleared() {
        if (File(applicationContext().cacheDir.path + "/installer_cache/").deleteRecursively()) {
            Log.d(javaClass.name, "Installer cache cleared")
        }
        // TODO - think about it
        super.onCleared()
    }
}