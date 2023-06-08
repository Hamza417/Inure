package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.SharedLibraryModel
import app.simple.inure.util.FileUtils.toFile
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
                val isInstalled = packageManager.isPackageInstalled(packageInfo.packageName)

                for (lib in getPackageInfo(isInstalled).applicationInfo.sharedLibraryFiles) {
                    list.add(SharedLibraryModel(lib, lib.toFile().length()))
                }

                for (lib in packageInfo.applicationInfo.nativeLibraryDir.toFile().listFiles()!!) {
                    list.add(SharedLibraryModel(lib.name, lib.length()))
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

    private fun getPackageInfo(isInstalled: Boolean): PackageInfo {
        return if (isInstalled) {
            packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_SHARED_LIBRARY_FILES)!!
        } else {
            packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir, PackageManager.GET_SHARED_LIBRARY_FILES)!!
        }
    }
}