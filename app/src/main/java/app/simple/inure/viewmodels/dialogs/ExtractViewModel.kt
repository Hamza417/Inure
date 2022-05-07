package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.PermissionUtils.arePermissionsGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExtractViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val fileName = packageInfo.applicationInfo.name + " (" + packageInfo.versionName + ")"

    private var cleared = false
    private val buffer = 2048
    private val byteArray = ByteArray(buffer)

    private val progress: MutableLiveData<Long> = MutableLiveData<Long>()
    private val status: MutableLiveData<String> = MutableLiveData<String>()
    private val error: MutableLiveData<String> = MutableLiveData<String>()
    private val success: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        extractAppFile()
    }

    fun getProgress(): LiveData<Long> {
        return progress
    }

    fun getStatus(): LiveData<String> {
        return status
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun getSuccess(): LiveData<Boolean> {
        return success
    }

    private fun extractAppFile() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                if (!context.arePermissionsGranted(MainPreferences.getStoragePermissionUri())) {
                    throw SecurityException("Storage Permission not granted")
                }

                val uri = Uri.parse(MainPreferences.getStoragePermissionUri())
                val pickedDir = DocumentFile.fromTreeUri(getApplication(), uri)
                var total = 0L
                var length = File(packageInfo.applicationInfo.sourceDir).length()

                if (packageInfo.applicationInfo.splitSourceDirs.isNotNull()) { // For split packages
                    status.postValue(getString(R.string.split_apk_detected))
                    val documentFile = pickedDir!!.createFile("application/zip", fileName)

                    val list = arrayOfNulls<String>(packageInfo.applicationInfo.splitSourceDirs.size + 1)

                    for (i in packageInfo.applicationInfo.splitSourceDirs.indices) {
                        list[i] = packageInfo.applicationInfo.splitSourceDirs[i]
                        length += File(packageInfo.applicationInfo.splitSourceDirs[i]).length()
                    }

                    list[list.size - 1] = packageInfo.applicationInfo.sourceDir

                    status.postValue(getString(R.string.creating_split_package))

                    contentResolver.openOutputStream(documentFile!!.uri, "w").use { outputStream ->
                        BufferedOutputStream(outputStream).use { bufferedOutputStream ->
                            ZipOutputStream(bufferedOutputStream).use { zipOutputStream ->
                                for (file in list) {
                                    val entry = ZipEntry(file?.substring(file.lastIndexOf("/") + 1))

                                    FileInputStream(file).use { fileInputStream ->
                                        BufferedInputStream(fileInputStream, buffer).use { bufferedInputStream ->
                                            zipOutputStream.putNextEntry(entry)
                                            var count: Int
                                            while (bufferedInputStream.read(byteArray, 0, buffer).also { count = it } != -1) {
                                                if (cleared) break
                                                total += count
                                                progress.postValue(total * 100 / length)
                                                zipOutputStream.write(byteArray, 0, count)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else { // For APK files
                    status.postValue(getString(R.string.preparing_apk_file))
                    val documentFile = pickedDir!!.createFile("application/vnd.android.package-archive", fileName)
                    val lengthOfFile = File(packageInfo.applicationInfo.sourceDir).length()

                    FileInputStream(packageInfo.applicationInfo.sourceDir).use { fileInputStream ->
                        contentResolver.openOutputStream(documentFile!!.uri, "w").use { outputStream ->
                            var len: Int
                            while (fileInputStream.read(byteArray).also { len = it } > 0) {
                                if (cleared) break
                                total += len
                                progress.postValue(total * 100 / lengthOfFile)
                                outputStream!!.write(byteArray, 0, len)
                            }
                        }
                    }
                }
            }.onFailure {
                it.printStackTrace()
                error.postValue(it.stackTraceToString())
            }.onSuccess {
                success.postValue(true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleared = true
    }
}