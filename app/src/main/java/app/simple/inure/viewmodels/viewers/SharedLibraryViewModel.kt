package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.model.SharedLibraryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharedLibraryViewModel(application: Application, val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val sharedLibraryModel: MutableLiveData<SharedLibraryModel> by lazy {
        MutableLiveData<SharedLibraryModel>().also {
            loadSharedLibs()
        }
    }

    private fun loadSharedLibs() {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }
}