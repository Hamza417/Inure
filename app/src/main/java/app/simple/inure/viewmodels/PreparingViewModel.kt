package app.simple.inure.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.util.NullSafety.isNotNull
import kotlinx.coroutines.*
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PreparingViewModel(application: Application) : AndroidViewModel(application) {

    private val progress: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            getApplication<Application>().getString(R.string.cache_dir)
        }
    }

    private val file: MutableLiveData<File?> by lazy {
        MutableLiveData<File?>()
    }

    fun getProgress(): LiveData<String> {
        return progress
    }

    fun getFile(): LiveData<File?> {
        return file
    }

    fun prepareApplicationFiles(applicationInfo: ApplicationInfo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    val file: File?

                    File(getApplication<Application>().getExternalFilesDir(null)!!.path + "/send_cache/").mkdir()

                    progress.postValue(getApplication<Application>().getString(R.string.cache_dir))

                    if (applicationInfo.splitSourceDirs.isNotNull()) {

                        progress.postValue(getApplication<Application>().getString(R.string.split_apk_detected))

                        file = File(getApplication<Application>().getExternalFilesDir(null)!!.path + "/send_cache/" + applicationInfo.name + ".zip")

                        if (!file.exists()) {

                            progress.postValue(getApplication<Application>().getString(R.string.creating_split_package))

                            val list = arrayOfNulls<String>(applicationInfo.splitSourceDirs.size)

                            for (i in applicationInfo.splitSourceDirs.indices) {
                                list[i] = applicationInfo.splitSourceDirs[i]
                                println(applicationInfo.splitSourceDirs[i])
                            }

                            list[list.size - 1] = applicationInfo.sourceDir
                            createZip(list.requireNoNulls(), file)
                        }

                    } else {
                        progress.postValue(getApplication<Application>().getString(R.string.preparing_apk_file))
                        file = File(getApplication<Application>().getExternalFilesDir(null)!!.path + "/send_cache/" + applicationInfo.name + ".apk")

                        if (!file.exists()) {
                            applicationInfo.sourceDir.copyTo(file)
                        }
                    }

                    progress.postValue(getApplication<Application>().getString(R.string.done))

                    this@PreparingViewModel.file.postValue(file)

                }.getOrElse { e ->
                    e.printStackTrace()
                    this@PreparingViewModel.file.postValue(null)
                }
            }
        }
    }

    private fun createZip(_files: Array<String>, zipFileName: File?) {
        kotlin.runCatching {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFileName))).use { zipOutputStream ->
                val data = ByteArray(BUFFER)
                for (i in _files.indices) {
                    Log.v("Compress", "Adding: " + _files[i])
                    val fi = FileInputStream(_files[i])

                    BufferedInputStream(fi, BUFFER).use { bufferInputStream ->
                        val entry = ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1))
                        zipOutputStream.putNextEntry(entry)
                        var count: Int
                        while (bufferInputStream.read(data, 0, BUFFER).also { count = it } != -1) {
                            zipOutputStream.write(data, 0, count)
                        }
                    }
                }
            }
        }.getOrElse {
            file.postValue(null)
        }
    }

    /**
     * Copy the given file to the destination [File]
     */
    private fun String.copyTo(destination: File) {
        kotlin.runCatching {
            FileInputStream(File(this)).use { `in` ->
                FileOutputStream(destination).use { out ->
                    val buf = ByteArray(BUFFER)
                    var len: Int
                    while (`in`.read(buf).also { len = it } > 0) {
                        out.write(buf, 0, len)
                    }
                }
            }
        }.getOrElse {
            file.postValue(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    companion object {
        private const val BUFFER = 1024
    }
}
