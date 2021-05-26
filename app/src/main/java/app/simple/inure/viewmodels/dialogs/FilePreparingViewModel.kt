package app.simple.inure.viewmodels.dialogs

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

class FilePreparingViewModel(application: Application, val applicationInfo: ApplicationInfo) : AndroidViewModel(application) {

    private var zipOutputStream: ZipOutputStream? = null
    private var fileOutputStream: FileOutputStream? = null
    private var fileInputStream: FileInputStream? = null
    private var bufferedOutputStream: BufferedOutputStream? = null
    private var bufferedInputStream: BufferedInputStream? = null
    private var cleared = false

    private val progress: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }

    private val status: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            getApplication<Application>().getString(R.string.cache_dir)
        }
    }

    private val file: MutableLiveData<File?> by lazy {
        MutableLiveData<File?>().also {
            prepareApplicationFiles()
        }
    }

    fun getProgress(): LiveData<Long> {
        return progress
    }

    fun getStatus(): LiveData<String> {
        return status
    }

    fun getFile(): LiveData<File?> {
        return file
    }

    private fun prepareApplicationFiles() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    val file: File?

                    File(getApplication<Application>().getExternalFilesDir(null)!!.path + "/send_cache/").mkdir()

                    status.postValue(getApplication<Application>().getString(R.string.cache_dir))

                    if (applicationInfo.splitSourceDirs.isNotNull()) {

                        status.postValue(getApplication<Application>().getString(R.string.split_apk_detected))

                        file = File(getApplication<Application>().getExternalFilesDir(null)!!.path + "/send_cache/" + applicationInfo.name + ".zip")

                        status.postValue(getApplication<Application>().getString(R.string.creating_split_package))

                        val list = arrayOfNulls<String>(applicationInfo.splitSourceDirs.size + 1)
                        var length = File(applicationInfo.sourceDir).length()

                        for (i in applicationInfo.splitSourceDirs.indices) {
                            list[i] = applicationInfo.splitSourceDirs[i]
                            length += File(applicationInfo.splitSourceDirs[i]).length()
                        }

                        if (!file.exists() || file.length() < length) {
                            list[list.size - 1] = applicationInfo.sourceDir
                            createZip(list.requireNoNulls(), file, length)
                        } else {
                            this@FilePreparingViewModel.file.postValue(file)
                        }

                    } else {
                        status.postValue(getApplication<Application>().getString(R.string.preparing_apk_file))
                        file = File(getApplication<Application>().getExternalFilesDir(null)!!.path + "/send_cache/" + applicationInfo.name + ".apk")

                        if (!file.exists() || file.length() < File(applicationInfo.sourceDir).length()) {
                            applicationInfo.sourceDir.copyTo(file)
                        } else {
                            this@FilePreparingViewModel.file.postValue(file)
                        }
                    }

                    status.postValue(getApplication<Application>().getString(R.string.done))

                    this@FilePreparingViewModel.file.postValue(file)

                }.getOrElse { e ->
                    e.printStackTrace()
                    this@FilePreparingViewModel.file.postValue(null)
                }
            }
        }
    }

    private fun createZip(_files: Array<String>, zipFileName: File?, length: Long) {
        kotlin.runCatching {
            var total = 0L
            fileOutputStream = FileOutputStream(zipFileName)
            bufferedOutputStream = BufferedOutputStream(fileOutputStream)
            zipOutputStream = ZipOutputStream(bufferedOutputStream)

            val data = ByteArray(BUFFER)

            for (i in _files.indices) {
                Log.v("Compress", "Adding: " + _files[i])
                fileInputStream = FileInputStream(_files[i])
                bufferedInputStream = BufferedInputStream(fileInputStream, BUFFER)

                val entry = ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1))
                zipOutputStream!!.putNextEntry(entry)
                var count: Int
                while (bufferedInputStream!!.read(data, 0, BUFFER).also { count = it } != -1) {
                    if(cleared) break
                    total += count
                    progress.postValue(total * 100 / length)
                    zipOutputStream!!.write(data, 0, count)
                }
            }

            zipOutputStream?.flush()
            bufferedOutputStream?.flush()
            fileOutputStream?.flush()
            zipOutputStream?.close()
            bufferedOutputStream?.close()
            fileOutputStream?.close()
            bufferedInputStream?.close()
            fileInputStream?.close()

        }.getOrElse {

            zipOutputStream?.flush()
            bufferedOutputStream?.flush()
            fileOutputStream?.flush()
            zipOutputStream?.close()
            bufferedOutputStream?.close()
            fileOutputStream?.close()
            bufferedInputStream?.close()
            fileInputStream?.close()

            file.postValue(null)
        }
    }

    /**
     * Copy the given file to the destination [File]
     */
    private fun String.copyTo(destination: File) {
        kotlin.runCatching {
            var total = 0L
            val lengthOfFile = File(this).length()

            fileInputStream = FileInputStream(File(this))
            fileOutputStream = FileOutputStream(destination)

            val buf = ByteArray(BUFFER)
            var len: Int
            while (fileInputStream!!.read(buf).also { len = it } > 0) {
                if(cleared) break
                total += len
                progress.postValue(total * 100 / lengthOfFile)
                fileOutputStream!!.write(buf, 0, len)
            }

            fileOutputStream!!.flush()
            fileOutputStream!!.close()
            fileInputStream!!.close()

        }.getOrElse {
            it.printStackTrace()

            fileOutputStream!!.flush()
            fileOutputStream!!.close()
            fileInputStream!!.close()

            file.postValue(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleared = true

        kotlin.runCatching {
            viewModelScope.cancel()
            zipOutputStream?.flush()
            bufferedOutputStream?.flush()
            fileOutputStream?.flush()
            zipOutputStream?.close()
            bufferedOutputStream?.close()
            fileOutputStream?.close()
            bufferedInputStream?.close()
            fileInputStream?.close()
        }.getOrElse {
            it.printStackTrace()
        }
        println("Cleared")
    }

    companion object {
        private const val BUFFER = 1024
    }
}
