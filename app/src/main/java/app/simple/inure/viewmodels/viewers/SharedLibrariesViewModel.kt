package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.SharedLibraryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharedLibrariesViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    val sharedLibraries: MutableLiveData<MutableList<SharedLibraryModel>> by lazy {
        MutableLiveData<MutableList<SharedLibraryModel>>().also {
            loadSharedLibs()
        }
    }

    val error: MutableLiveData<String> = MutableLiveData<String>()

    private fun loadSharedLibs() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val list = arrayListOf<SharedLibraryModel>()

                for (lib in packageInfo.applicationInfo.sharedLibraryFiles) {
                    list.add(SharedLibraryModel(lib))
                }

                this@SharedLibrariesViewModel.sharedLibraries.postValue(list)
            }.getOrElse {
                it.printStackTrace()
                error.postValue(it.stackTraceToString())
            }
        }
    }
}