package app.simple.inure.viewmodels.installer

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.installer.InstallerUtils
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.apk.utils.PackageData.getInstallerDir
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.FileUtils
import com.anggrayudi.storage.file.baseName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import net.lingala.zip4j.ZipFile
import java.io.File

class InstallerViewModel(application: Application, private val uri: Uri) : WrappedViewModel(application) {

    private val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        PackageManager.GET_META_DATA or
                PackageManager.GET_SERVICES or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SIGNING_CERTIFICATES or
                PackageManager.GET_SHARED_LIBRARY_FILES
    } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_META_DATA or
                PackageManager.GET_SERVICES or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SIGNATURES or
                PackageManager.GET_SHARED_LIBRARY_FILES
    }

    private val packageInfo: MutableLiveData<PackageInfo> by lazy {
        MutableLiveData<PackageInfo>().also {
            viewModelScope.launch(Dispatchers.Default) {
                prepareInstallation()
            }
        }
    }

    private val file: MutableLiveData<File> by lazy {
        MutableLiveData<File>()
    }

    fun getPackageInfo(): LiveData<PackageInfo> {
        return packageInfo
    }

    fun getFile(): LiveData<File> {
        return file
    }

    private fun prepareInstallation() {
        kotlin.runCatching {
            PackageData.makePackageFolder(applicationContext())

            uri.let { it ->
                val name = DocumentFile.fromSingleUri(applicationContext(), it)!!
                val sourceFile = applicationContext().getInstallerDir(name.baseName + ".zip")

                if (!sourceFile.exists()) {
                    contentResolver.openInputStream(it).use {
                        FileUtils.copyStreamToFile(it!!, sourceFile)
                    }
                }

                if (name.name!!.endsWith(".apkm")) {
                    if (!sourceFile.exists()) {
                        contentResolver.openInputStream(it).use {
                            FileUtils.copyStreamToFile(it!!, sourceFile)
                        }
                    }

                    ZipFile(sourceFile.path).extractAll(sourceFile.path.substringBeforeLast("."))
                } else {
                    val p = packageManager.getPackageArchiveInfo(sourceFile.path, flags)!!

                    ApkFile(sourceFile).use {
                        p.applicationInfo.name = it.apkMeta.label
                    }

                    this.file.postValue(sourceFile)
                    packageInfo.postValue(p)
                }
            }
        }.getOrElse {
            error.postValue(it.stackTraceToString())
        }
    }

    private fun prepareBundleInstallation() {
        uri.let {

        }
    }

    fun install() {
        viewModelScope.launch(Dispatchers.Default) {
            val file = this@InstallerViewModel.file.value!!
            val sessionParams = InstallerUtils.makeInstallParams(file.length())
            val sessionCode = InstallerUtils.createSession(sessionParams, applicationContext())

            // TODO create a loop for split apks
            if (file.exists() && file.name.endsWith(".apk")) {
                InstallerUtils.installWriteSessions(sessionCode, file, applicationContext())
            }

            InstallerUtils.commitSession(sessionCode, applicationContext())
        }
    }
}