package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.exceptions.LargeStringException
import app.simple.inure.extensions.viewmodels.RootServiceViewModel
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.XMLUtils.formatXML
import app.simple.inure.util.XMLUtils.getPrettyXML
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.charset.Charset

class SharedPreferencesViewerViewModel(private val pathToXml: String, private val packageInfo: PackageInfo, application: Application) : RootServiceViewModel(application) {

    init {
        initRootProc()
    }

    private val spanned: MutableLiveData<Spanned> by lazy {
        MutableLiveData<Spanned>()
    }

    private val loaderCode: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun getSpanned(): LiveData<Spanned> {
        return spanned
    }

    fun getLoaderCode(): LiveData<Int> {
        return loaderCode
    }

    private fun loadSharedPrefsFile(fileSystemManager: FileSystemManager?) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val code = fileSystemManager?.getSharedPrefsString()!!

                if (code.length >= 150000 && !FormattingPreferences.isLoadingLargeStrings()) {
                    throw LargeStringException("String size ${code.length} is too big to render without freezing the app")
                }

                spanned.postValue(code.formatXML().getPrettyXML())
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun FileSystemManager.getSharedPrefsString(): String {
        val channel = openChannel(pathToXml, FileSystemManager.MODE_READ_WRITE)
        val capacity = channel.size()
        val byteBuffer = ByteBuffer.allocate(capacity.toInt())
        channel.read(byteBuffer)
        channel.close()
        return String(byteBuffer.array(), Charset.defaultCharset())
    }

    override fun runRootProcess(fileSystemManager: FileSystemManager?) {
        loadSharedPrefsFile(fileSystemManager)
    }

    @Suppress("unused")
    fun replacePreferences(text: String, requestCode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                Shell.cmd("cp -f $pathToXml $pathToXml.bak").exec()
                Shell.cmd("rm -f $pathToXml").exec()
                Shell.cmd("touch $pathToXml").exec()
                Shell.cmd("echo \"$text\" > $pathToXml").exec()
                Shell.cmd("chmod 660 $pathToXml").exec()
                loaderCode.postValue(requestCode)
            }.onFailure {
                postWarning(it.message)
                loaderCode.postValue(-1)
            }
        }
    }

    fun writePreferencesTextToFile(text: String, requestCode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                // Force close the app first
                Shell.cmd("am force-stop ${packageInfo.packageName}").exec()

                val extendedFile = getFileSystemManager()?.getFile(pathToXml)
                val outputStream = extendedFile?.newOutputStream()
                outputStream?.write(text.toByteArray())
                outputStream?.close()

                // Set the permissions of the file to 660
                Shell.cmd("chmod 660 $pathToXml").exec()

                loaderCode.postValue(requestCode)
            }.onFailure {
                it.printStackTrace()
                postWarning(it.message)
                loaderCode.postValue(-1)
            }
        }
    }
}