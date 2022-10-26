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

    private fun loadSharedLibs() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val list = arrayListOf<SharedLibraryModel>()

                for (lib in packageInfo.applicationInfo.sharedLibraryFiles) {
                    list.add(SharedLibraryModel(lib))
                }

                this@SharedLibrariesViewModel.sharedLibraries.postValue(list)
            }.getOrElse {
                if (it is NullPointerException) {
                    notFound.postValue((0..100).random())
                } else {
                    postError(it)
                }
            }
        }
    }
}