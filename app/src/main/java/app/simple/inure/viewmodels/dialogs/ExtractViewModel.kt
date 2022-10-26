package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.BatchUtils.getApkPathAndFileName
import app.simple.inure.util.BatchUtils.getBundlePathAndFileName
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.PermissionUtils.areStoragePermissionsGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.*

class ExtractViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val progress: MutableLiveData<Long> = MutableLiveData<Long>()
    private val status: MutableLiveData<String> = MutableLiveData<String>()
    private val success: MutableLiveData<Boolean> = MutableLiveData(false)
    private val file: MutableLiveData<File?> = MutableLiveData<File?>()

    init {
        extractAppFile()
    }

    fun getProgress(): LiveData<Long> {
        return progress
    }

    fun getStatus(): LiveData<String> {
        return status
    }

    fun getSuccess(): LiveData<Boolean> {
        return success
    }

    fun getFile(): LiveData<File?> {
        return file
    }

    private fun extractAppFile() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                if (context.areStoragePermissionsGranted()) {
                    PackageData.makePackageFolder(applicationContext())
                } else {
                    throw SecurityException("Storage Permission not granted")
                }

                if (packageInfo.applicationInfo.splitSourceDirs.isNotNull()) { // For split packages
                    status.postValue(getString(R.string.split_apk_detected))
                    extractBundle()
                } else { // For APK files
                    status.postValue(getString(R.string.preparing_apk_file))
                    extractApk()
                }
            }.onFailure {
                postError(it)
            }.onSuccess {
                success.postValue(true)
            }
        }
    }

    private fun extractBundle() {
        kotlin.runCatching {
            if (!File(applicationContext().getBundlePathAndFileName(packageInfo)).exists()) {
                status.postValue(getString(R.string.creating_split_package))
                val zipFile = ZipFile(applicationContext().getBundlePathAndFileName(packageInfo))
                val progressMonitor = zipFile.progressMonitor

                zipFile.isRunInThread = true
                zipFile.addFiles(createSplitApkFiles())

                while (!progressMonitor.state.equals(ProgressMonitor.State.READY)) {
                    progress.postValue(progressMonitor.percentDone.toLong())
                }

                if (progressMonitor.result.equals(ProgressMonitor.Result.ERROR)) {
                    postError(progressMonitor.exception)
                } else if (progressMonitor.result.equals(ProgressMonitor.Result.CANCELLED)) {
                    status.postValue(getString(R.string.cancelled))
                }
            }
        }.onFailure {
            postError(it)
        }.onSuccess {
            file.postValue(File(applicationContext().getBundlePathAndFileName(packageInfo)))
        }
    }

    @Throws(IOException::class)
    private fun extractApk() {
        if (File(PackageData.getPackageDir(applicationContext()), getApkPathAndFileName(packageInfo)).exists()) {
            file.postValue(File(PackageData.getPackageDir(applicationContext()), getApkPathAndFileName(packageInfo)))
        } else {
            val source = File(packageInfo.applicationInfo.sourceDir)
            val dest = File(PackageData.getPackageDir(applicationContext()), getApkPathAndFileName(packageInfo))
            val length = source.length()

            val inputStream = FileInputStream(source)
            val outputStream = FileOutputStream(dest)

            copyStream(inputStream, outputStream, length)

            inputStream.close()
            outputStream.close()

            file.postValue(File(PackageData.getPackageDir(applicationContext()), getApkPathAndFileName(packageInfo)))
        }
    }

    @Throws(IOException::class)
    fun copyStream(from: InputStream, to: OutputStream, length: Long) {
        val buf = ByteArray(1024 * 1024)
        var len: Int
        var total = 0L
        while (from.read(buf).also { len = it } > 0) {
            to.write(buf, 0, len)
            total += len
            progress.postValue(total * 100 / length)
        }
    }

    private fun createSplitApkFiles(): ArrayList<File> {
        val list = arrayListOf<File>()

        list.add(File(packageInfo.applicationInfo.sourceDir))

        for (i in packageInfo.applicationInfo.splitSourceDirs.indices) {
            list.add(File(packageInfo.applicationInfo.splitSourceDirs[i]))
        }

        return list
    }
}