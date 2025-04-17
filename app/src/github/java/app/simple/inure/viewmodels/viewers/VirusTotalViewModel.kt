package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.virustotal.VirusTotalClient
import app.simple.inure.virustotal.VirusTotalResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class VirusTotalViewModel(application: Application, private val packageInfo: PackageInfo) : WrappedViewModel(application) {

    init {
        loadVirusTotalData()
    }

    private val response: MutableLiveData<JSONObject> by lazy {
        MutableLiveData<JSONObject>()
    }

    private val progress: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val failed: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getResponse(): LiveData<JSONObject> {
        return response
    }

    fun getProgress(): LiveData<String> {
        return progress
    }

    fun getFailed(): LiveData<String> {
        return failed
    }

    private fun loadVirusTotalData() {
        viewModelScope.launch(Dispatchers.IO) {
            VirusTotalClient.getInstance().scanFile(packageInfo.applicationInfo?.sourceDir!!).collect { virusTotalResult ->
                when (virusTotalResult) {
                    is VirusTotalResult.Error -> {
                        failed.postValue(virusTotalResult.message)
                    }
                    is VirusTotalResult.Progress -> {
                        progress.postValue(virusTotalResult.status)
                    }
                    is VirusTotalResult.Success -> {
                        val json = virusTotalResult.result
                        response.postValue(json)
                    }
                }
            }
        }
    }
}