package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.Bloat
import app.simple.inure.utils.DebloatUtils.getBloatInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebloatInfoViewModel(application: Application, packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val bloat: MutableLiveData<Bloat> by lazy {
        MutableLiveData<Bloat>().also {
            loadBloatInfo(packageInfo)
        }
    }

    fun getBloat(): LiveData<Bloat> {
        return bloat
    }

    private fun loadBloatInfo(packageInfo: PackageInfo) {
        viewModelScope.launch(Dispatchers.Default) {
            bloat.postValue(packageInfo.getBloatInfo().apply {
                this?.packageInfo = packageInfo
            })
        }
    }
}