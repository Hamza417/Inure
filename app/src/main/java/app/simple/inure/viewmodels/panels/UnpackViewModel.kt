package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.UnpackConstants
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.UnpackModel
import app.simple.inure.util.FileSizeHelper.toSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class UnpackViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    var zipFile: ZipFile? = null

    private val listData: MutableLiveData<ArrayList<UnpackModel>> by lazy {
        MutableLiveData<ArrayList<UnpackModel>>().also {
            loadFiles()
        }
    }

    fun getFileData(): LiveData<ArrayList<UnpackModel>> {
        return listData
    }

    private fun loadFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            zipFile = ZipFile(packageInfo.applicationInfo.sourceDir)
            val entries: Enumeration<out ZipEntry?> = zipFile?.entries()!!
            val list = arrayListOf<UnpackModel>()

            while (entries.hasMoreElements()) {
                val unpackModel = UnpackModel()
                val entry: ZipEntry? = entries.nextElement()

                if (entry?.isDirectory == true) {
                    unpackModel.type = UnpackConstants.FOLDER
                } else {
                    unpackModel.type = UnpackConstants.FILE
                }

                kotlin.runCatching {
                    unpackModel.name = entry?.name?.substring(entry.name?.lastIndexOf("/")?.plus(1)!!)
                }.getOrElse {
                    unpackModel.name = entry?.name
                }

                unpackModel.path = entry?.name
                unpackModel.size = entry?.size?.toSize()

                list.add(unpackModel)
            }

            listData.postValue(list)
        }
    }

    override fun onCleared() {
        super.onCleared()
        zipFile?.close()
    }
}