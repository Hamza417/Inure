package app.simple.inure.viewmodels.installer

import android.app.Application
import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.decoders.XMLDecoder
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.XMLUtils.formatXML
import app.simple.inure.util.XMLUtils.getPrettyXML
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipException

class InstallerManifestViewModel(application: Application, private val file: File) : WrappedViewModel(application) {

    private val spanned: MutableLiveData<Spanned> by lazy {
        MutableLiveData<Spanned>().also {
            getSpannedXml()
        }
    }

    fun getSpanned(): LiveData<Spanned> {
        return spanned
    }

    private fun getSpannedXml() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val code: String = try {
                    XMLDecoder(file)
                        .decode(APKParser.ANDROID_MANIFEST)
                } catch (e: ZipException) {
                    val byteBuffer: ByteBuffer = APKParser
                        .getManifestByteBuffer(file)
                        .order(ByteOrder.LITTLE_ENDIAN)

                    XMLDecoder.decode(byteBuffer)
                }

                spanned.postValue(code.formatXML().getPrettyXML())
            }.getOrElse {
                it.printStackTrace()
            }
        }
    }
}