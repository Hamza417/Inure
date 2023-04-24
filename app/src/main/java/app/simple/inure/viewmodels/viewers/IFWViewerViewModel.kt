package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.text.Spannable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.RootServiceViewModel
import app.simple.inure.util.XMLUtils.formatXML
import app.simple.inure.util.XMLUtils.getPrettyXML
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.charset.Charset

class IFWViewerViewModel(application: Application, packageInfo: PackageInfo) : RootServiceViewModel(application) {

    private val pathToXml = "/data/system/ifw/${packageInfo.packageName}.xml"

    private val xml: MutableLiveData<Spannable> by lazy {
        MutableLiveData<Spannable>().also {
            initRootProc()
        }
    }

    fun getXML(): MutableLiveData<Spannable> {
        return xml
    }

    override fun runRootProcess(fileSystemManager: FileSystemManager?) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val code = fileSystemManager?.getXML()!!
                xml.postValue(code.formatXML().getPrettyXML())
            }.getOrElse {
                postWarning(it.message.toString())
            }
        }
    }

    private fun FileSystemManager.getXML(): String {
        val channel = openChannel(pathToXml, FileSystemManager.MODE_READ_WRITE)
        val capacity = channel.size()
        val byteBuffer = ByteBuffer.allocate(capacity.toInt())
        channel.read(byteBuffer)
        channel.close()
        return String(byteBuffer.array(), Charset.defaultCharset())
    }

    fun saveXML(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val extendedFile = getFileSystemManager()?.getFile(pathToXml)
                    val outputStream = extendedFile?.newOutputStream()
                    outputStream?.write(text.toByteArray())
                    outputStream?.close()

                    // Set the permissions of the file to 660
                    Shell.cmd("chmod 660 $pathToXml").exec()
                    postWarning(getString(R.string.saved_successfully))
                }.onFailure {
                    it.printStackTrace()
                    postWarning(it.message)
                }
            }
        }
    }
}