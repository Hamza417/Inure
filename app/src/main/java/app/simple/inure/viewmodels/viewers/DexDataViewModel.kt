package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.dex.Dex
import app.simple.inure.apk.dex.DexClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DexDataViewModel(application: Application, private val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val dexData: MutableLiveData<MutableList<DexClass>> by lazy {
        MutableLiveData<MutableList<DexClass>>().also {
            loadDexData()
        }
    }

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getDexClasses(): LiveData<MutableList<DexClass>> {
        return dexData
    }

    fun getError(): LiveData<String> {
        return error
    }

    private fun loadDexData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                Dex(packageInfo.applicationInfo.sourceDir).use {
                    dexData.postValue(it.dexClasses.toMutableList())
                }
            }.getOrElse {
                error.postValue(it.stackTraceToString())
            }
        }
    }
}