package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser.getDexClasses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.bean.DexClass

class DexDataViewModel(application: Application, private val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val dexData: MutableLiveData<ArrayList<DexClass>> by lazy {
        MutableLiveData<ArrayList<DexClass>>().also {
            loadDexData()
        }
    }

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getDexClasses(): LiveData<ArrayList<DexClass>> {
        return dexData
    }

    fun getError(): LiveData<String> {
        return error
    }

    private fun loadDexData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                dexData.postValue(packageInfo.getDexClasses())
            }.getOrElse {
                error.postValue(it.message!!)
            }
        }
    }
}